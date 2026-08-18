package cn.org.starpivot.ai.service.chat;

import cn.org.starpivot.ai.agent.CurrentDateTimeTool;
import cn.org.starpivot.ai.agent.KnowledgeSearchTool;
import cn.org.starpivot.ai.config.AiPromptTemplateSnapshot;
import cn.org.starpivot.ai.config.AiRuntimeSnapshot;
import cn.org.starpivot.ai.domain.dto.ChatSendDto;
import cn.org.starpivot.ai.domain.vo.RagRetrievalResult;
import cn.org.starpivot.ai.memory.ConversationSummaryService;
import cn.org.starpivot.ai.provider.AiModelClientFactory;
import cn.org.starpivot.ai.provider.AiModelRef;
import cn.org.starpivot.ai.service.AiKnowledgeRetrievalService;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.security.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatPromptAssembler {

    private final AiModelClientFactory aiModelClientFactory;
    private final MessageWindowChatMemory chatMemory;
    private final AiKnowledgeRetrievalService aiKnowledgeRetrievalService;
    private final SystemPromptResolver systemPromptResolver;
    private final ConversationSummaryService conversationSummaryService;
    private final KnowledgeSearchTool knowledgeSearchTool;
    private final CurrentDateTimeTool currentDateTimeTool;

    public ChatClient.ChatClientRequestSpec buildPrompt(
            ChatSendDto dto,
            String conversationId,
            AiRuntimeSnapshot runtime,
            RagRetrievalResult ragResult,
            ChatExecutionPlan plan) {
        systemPromptResolver.assertSceneAllowed(runtime, plan.promptScene());

        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder();
        String apiModel = AiModelRef.modelId(plan.model());
        if (StringUtils.hasText(apiModel)) {
            optionsBuilder.model(apiModel);
        }
        Double temperature = resolveTemperature(dto.getTemperature(), plan.promptScene(), runtime);
        if (temperature != null) {
            optionsBuilder.temperature(temperature);
        }
        if (plan.useAgent()) {
            optionsBuilder.internalToolExecutionEnabled(true);
        }

        boolean hasStrongRag = hasStrongRag(ragResult);
        ChatClient.ChatClientRequestSpec spec = aiModelClientFactory.chatClient(plan.model())
                .prompt()
                .options(optionsBuilder.build())
                .system(buildSystemPrompt(runtime, conversationId, plan, ragResult, hasStrongRag))
                .user(dto.getMessage())
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId));
        if (plan.useAgent()) {
            // 预检索已足够时不再挂知识库工具，避免二次检索互相打架
            if (hasStrongRag) {
                spec = spec.tools(currentDateTimeTool)
                        .toolContext(buildToolContext(conversationId, runtime));
            } else {
                spec = spec.tools(knowledgeSearchTool, currentDateTimeTool)
                        .toolContext(buildToolContext(conversationId, runtime));
            }
        }
        return spec;
    }

    public String resolveModel(String requestedModel, AiRuntimeSnapshot runtime, String promptScene) {
        AiPromptTemplateSnapshot scene = systemPromptResolver.findTemplate(runtime, promptScene);
        return resolveModel(requestedModel, scene, runtime);
    }

    public RagRetrievalResult retrieve(AiRuntimeSnapshot runtime, String userMessage, boolean useRag) {
        if (!useRag || !runtime.isRagEnabled() || !StringUtils.hasText(userMessage)) {
            return RagRetrievalResult.builder().context("").sources(java.util.List.of()).build();
        }
        // 仅具备知识库查询权限的用户可检索全局知识库，避免任意对话用户读出知识内容
        if (!SecurityContextUtils.hasAuthority("ai:knowledge:query")) {
            log.debug("Skip RAG: current user lacks ai:knowledge:query");
            return RagRetrievalResult.builder().context("").sources(java.util.List.of()).build();
        }
        try {
            return aiKnowledgeRetrievalService.retrieve(userMessage, runtime.getRagTopK());
        } catch (RuntimeException ex) {
            log.warn("RAG retrieval failed, continue without knowledge context: {}", ex.getMessage());
            return RagRetrievalResult.builder().context("").sources(java.util.List.of()).degraded(true).build();
        }
    }

    public String resolveModel(String requestedModel, AiRuntimeSnapshot runtime) {
        return resolveModel(requestedModel, null, runtime);
    }

    private String resolveModel(String requestedModel, AiPromptTemplateSnapshot scene, AiRuntimeSnapshot runtime) {
        if (StringUtils.hasText(requestedModel) && !ChatQueryRouter.AUTO.equalsIgnoreCase(requestedModel.trim())) {
            String model = requestedModel.trim();
            if (!runtime.isModelAllowed(model)) {
                throw new BizException("不支持的模型：" + model);
            }
            return canonicalModelId(runtime, model);
        }
        if (scene != null && StringUtils.hasText(scene.getModel()) && runtime.isModelAllowed(scene.getModel())) {
            return canonicalModelId(runtime, scene.getModel());
        }
        return runtime.getDefaultModel();
    }

    private static String canonicalModelId(AiRuntimeSnapshot runtime, String model) {
        if (runtime.getModels() == null || runtime.getModels().isEmpty()) {
            return model;
        }
        String bare = AiModelRef.modelId(model);
        for (var item : runtime.getModels()) {
            if (item == null || !StringUtils.hasText(item.getId())) {
                continue;
            }
            String id = item.getId().trim();
            if (model.equals(id) || (bare != null && bare.equals(AiModelRef.modelId(id)))) {
                return id;
            }
        }
        return model;
    }

    private Double resolveTemperature(Double requestedTemperature, String promptScene, AiRuntimeSnapshot runtime) {
        if (requestedTemperature != null) {
            return requestedTemperature;
        }
        AiPromptTemplateSnapshot scene = systemPromptResolver.findTemplate(runtime, promptScene);
        if (scene != null && scene.getTemperature() != null) {
            return scene.getTemperature();
        }
        return runtime.getDefaultTemperature();
    }

    private String buildSystemPrompt(
            AiRuntimeSnapshot runtime,
            String conversationId,
            ChatExecutionPlan plan,
            RagRetrievalResult ragResult,
            boolean hasStrongRag) {
        String scene = systemPromptResolver.normalizeSceneId(runtime, plan.promptScene());
        StringBuilder builder = new StringBuilder(systemPromptResolver.resolve(runtime, plan.promptScene()).trim());
        builder.append("\n\n").append(ResponseFormatGuide.forScene(scene));
        builder.append("\n\n").append(runtimeCatalogHint(runtime, plan));
        if (plan.useAgent()) {
            builder.append("\n\n").append(ResponseFormatGuide.agentInstruction(hasStrongRag));
        }
        String summary = conversationSummaryService.getSummary(conversationId);
        if (StringUtils.hasText(summary)) {
            builder.append("\n\n## 历史对话摘要\n").append(summary.trim());
        }
        if (hasStrongRag) {
            builder.append("\n\n").append(ResponseFormatGuide.ragInstruction());
            builder.append("\n\n").append(ragResult.getContext());
        } else if (plan.useRag()) {
            builder.append("\n\n").append(ResponseFormatGuide.emptyRagAbstain());
        }
        return builder.toString();
    }

    private static boolean hasStrongRag(RagRetrievalResult ragResult) {
        return ragResult != null
                && !ragResult.isDegraded()
                && StringUtils.hasText(ragResult.getContext());
    }

    private String runtimeCatalogHint(AiRuntimeSnapshot runtime, ChatExecutionPlan plan) {
        StringBuilder hint = new StringBuilder();
        hint.append("## 当前运行时（以此为准，忽略文档里过期的模型名）\n");
        if (StringUtils.hasText(plan.model())) {
            hint.append("- 本轮实际调用模型：`").append(plan.model().trim()).append("`\n");
        }
        if (StringUtils.hasText(runtime.getDefaultModel())) {
            hint.append("- 系统默认模型：`").append(runtime.getDefaultModel().trim()).append("`\n");
        }
        if (runtime.getModels() != null && !runtime.getModels().isEmpty()) {
            String catalog = runtime.getModels().stream()
                    .limit(12)
                    .map(item -> {
                        String id = item.getId();
                        String label = StringUtils.hasText(item.getLabel()) ? item.getLabel().trim() : id;
                        String vendor = StringUtils.hasText(item.getProviderName())
                                ? item.getProviderName().trim() + " / "
                                : "";
                        return vendor + label + " (`" + id + "`)";
                    })
                    .collect(java.util.stream.Collectors.joining("、"));
            hint.append("- 已接入：").append(catalog);
        }
        return hint.toString();
    }

    private Map<String, Object> buildToolContext(String conversationId, AiRuntimeSnapshot runtime) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("conversationId", conversationId);
        context.put("canQueryKnowledge", SecurityContextUtils.hasAuthority("ai:knowledge:query"));
        context.put("ragEnabled", runtime.isRagEnabled());
        context.put("ragTopK", runtime.getRagTopK());
        return context;
    }
}
