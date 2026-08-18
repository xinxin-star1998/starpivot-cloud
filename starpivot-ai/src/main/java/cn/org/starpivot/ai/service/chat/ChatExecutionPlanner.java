package cn.org.starpivot.ai.service.chat;

import cn.org.starpivot.ai.agent.AgentToolSupport;
import cn.org.starpivot.ai.config.AiProperties;
import cn.org.starpivot.ai.config.AiRuntimeSnapshot;
import cn.org.starpivot.ai.domain.dto.ChatSendDto;
import cn.org.starpivot.ai.memory.ConversationIntentStore;
import cn.org.starpivot.ai.memory.ConversationTopicStore;
import cn.org.starpivot.ai.metrics.AiMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatExecutionPlanner {

    private final ChatQueryRouter chatQueryRouter;
    private final ChatPromptAssembler chatPromptAssembler;
    private final SystemPromptResolver systemPromptResolver;
    private final ChatLlmIntentClassifier chatLlmIntentClassifier;
    private final AgentToolSupport agentToolSupport;
    private final ConversationIntentStore conversationIntentStore;
    private final ConversationTopicStore conversationTopicStore;
    private final AiProperties aiProperties;
    private final AiMetrics aiMetrics;

    public ChatExecutionPlan plan(ChatSendDto dto, AiRuntimeSnapshot runtime) {
        return plan(dto, runtime, dto.getConversationId());
    }

    public ChatExecutionPlan plan(ChatSendDto dto, AiRuntimeSnapshot runtime, String conversationId) {
        if (!aiProperties.getQueryRouter().isEnabled()) {
            return manualPlan(dto, runtime);
        }

        boolean autoScene = chatQueryRouter.isAuto(dto.getPromptScene());
        boolean autoModel = chatQueryRouter.isAuto(dto.getModel());
        if (!autoScene && !autoModel) {
            return manualPlan(dto, runtime);
        }

        ChatIntent previous = autoScene ? conversationIntentStore.load(conversationId) : null;
        ChatIntent intent = chatQueryRouter.classify(dto.getMessage(), previous);
        if (autoScene) {
            intent = chatLlmIntentClassifier.refine(dto.getMessage(), intent, previous, conversationId);
            conversationIntentStore.save(conversationId, intent);
        }
        if (intent != ChatIntent.CHITCHAT) {
            conversationTopicStore.rememberFromMessage(conversationId, dto.getMessage());
        }
        ChatQueryRouter.RoutedSuggestion suggestion = chatQueryRouter.suggest(intent, runtime);

        String promptScene = autoScene ? suggestion.promptScene() : dto.getPromptScene().trim();
        systemPromptResolver.assertSceneAllowed(runtime, promptScene);

        String model = resolveModel(dto, runtime, autoModel, autoScene, promptScene, suggestion.model());
        if (autoModel && !runtime.isModelAllowed(model)) {
            model = chatPromptAssembler.resolveModel(null, runtime, promptScene);
        }
        boolean useRag = runtime.isRagEnabled()
                && (autoScene
                        ? suggestion.useRag()
                                || chatQueryRouter.shouldUseRagForManualScene(promptScene, dto.getMessage())
                        : chatQueryRouter.shouldUseRagForManualScene(promptScene, dto.getMessage()));
        boolean useAgent = agentToolSupport.shouldEnable(intent, model);

        log.info("[QueryRouter] intent={} scene={} model={} useRag={} useAgent={} autoScene={} autoModel={}",
                intent, promptScene, model, useRag, useAgent, autoScene, autoModel);
        aiMetrics.recordQueryRoute(intent.name(), useRag, true);

        return ChatExecutionPlan.builder()
                .intent(intent)
                .promptScene(promptScene)
                .model(model)
                .useRag(useRag)
                .useAgent(useAgent)
                .autoScene(autoScene)
                .autoModel(autoModel)
                .build();
    }

    private ChatExecutionPlan manualPlan(ChatSendDto dto, AiRuntimeSnapshot runtime) {
        String promptScene = chatQueryRouter.isAuto(dto.getPromptScene())
                ? systemPromptResolver.normalizeSceneId(runtime, null)
                : systemPromptResolver.normalizeSceneId(runtime, dto.getPromptScene());
        systemPromptResolver.assertSceneAllowed(runtime, promptScene);
        String model = chatPromptAssembler.resolveModel(dto.getModel(), runtime, promptScene);
        boolean useRag = runtime.isRagEnabled()
                && chatQueryRouter.shouldUseRagForManualScene(promptScene, dto.getMessage());
        boolean useAgent = agentToolSupport.shouldEnable(ChatIntent.GENERAL, model);
        conversationTopicStore.rememberFromMessage(dto.getConversationId(), dto.getMessage());
        return ChatExecutionPlan.builder()
                .intent(ChatIntent.GENERAL)
                .promptScene(promptScene)
                .model(model)
                .useRag(useRag)
                .useAgent(useAgent)
                .autoScene(false)
                .autoModel(false)
                .build();
    }

    private String resolveModel(
            ChatSendDto dto,
            AiRuntimeSnapshot runtime,
            boolean autoModel,
            boolean autoScene,
            String promptScene,
            String routedModel) {
        if (!autoModel) {
            return chatPromptAssembler.resolveModel(dto.getModel(), runtime, promptScene);
        }
        if (!autoScene) {
            return chatPromptAssembler.resolveModel(null, runtime, promptScene);
        }
        if (StringUtils.hasText(routedModel) && runtime.isModelAllowed(routedModel)) {
            return routedModel;
        }
        return chatPromptAssembler.resolveModel(null, runtime, promptScene);
    }
}
