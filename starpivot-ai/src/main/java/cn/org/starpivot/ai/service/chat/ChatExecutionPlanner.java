package cn.org.starpivot.ai.service.chat;

import cn.org.starpivot.ai.config.AiProperties;
import cn.org.starpivot.ai.config.AiRuntimeSnapshot;
import cn.org.starpivot.ai.domain.dto.ChatSendDto;
import cn.org.starpivot.ai.metrics.AiMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatExecutionPlanner {

    private final ChatQueryRouter chatQueryRouter;
    private final ChatPromptAssembler chatPromptAssembler;
    private final SystemPromptResolver systemPromptResolver;
    private final AiProperties aiProperties;
    private final AiMetrics aiMetrics;

    public ChatExecutionPlan plan(ChatSendDto dto, AiRuntimeSnapshot runtime) {
        if (!aiProperties.getQueryRouter().isEnabled()) {
            return manualPlan(dto, runtime);
        }

        boolean autoScene = chatQueryRouter.isAuto(dto.getPromptScene());
        boolean autoModel = chatQueryRouter.isAuto(dto.getModel());
        if (!autoScene && !autoModel) {
            return manualPlan(dto, runtime);
        }

        ChatIntent intent = chatQueryRouter.classify(dto.getMessage());
        ChatQueryRouter.RoutedSuggestion suggestion = chatQueryRouter.suggest(intent);

        String promptScene = autoScene ? suggestion.promptScene() : dto.getPromptScene().trim();
        systemPromptResolver.assertSceneAllowed(runtime, promptScene);

        String model = resolveModel(dto, runtime, autoModel, autoScene, promptScene, suggestion.model());
        boolean useRag = runtime.isRagEnabled()
                && (autoScene
                        ? suggestion.useRag()
                        : chatQueryRouter.shouldUseRagForManualScene(promptScene, dto.getMessage()));

        log.info("[QueryRouter] intent={} scene={} model={} useRag={} autoScene={} autoModel={}",
                intent, promptScene, model, useRag, autoScene, autoModel);
        aiMetrics.recordQueryRoute(intent.name(), useRag, true);

        return ChatExecutionPlan.builder()
                .intent(intent)
                .promptScene(promptScene)
                .model(model)
                .useRag(useRag)
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
        return ChatExecutionPlan.builder()
                .intent(ChatIntent.GENERAL)
                .promptScene(promptScene)
                .model(model)
                .useRag(useRag)
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
        return chatPromptAssembler.resolveModel(routedModel, runtime, promptScene);
    }
}
