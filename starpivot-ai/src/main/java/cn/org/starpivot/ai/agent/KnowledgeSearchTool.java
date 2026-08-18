package cn.org.starpivot.ai.agent;

import cn.org.starpivot.ai.domain.vo.RagRetrievalResult;
import cn.org.starpivot.ai.metrics.AiMetrics;
import cn.org.starpivot.ai.rag.RagConversationalRewriter;
import cn.org.starpivot.ai.service.AiKnowledgeRetrievalService;
import cn.org.starpivot.ai.service.AiRuntimeConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeSearchTool {

    private final AiKnowledgeRetrievalService aiKnowledgeRetrievalService;
    private final AiRuntimeConfigService aiRuntimeConfigService;
    private final AgentSourceCollector agentSourceCollector;
    private final AgentToolStatusNotifier agentToolStatusNotifier;
    private final RagConversationalRewriter ragConversationalRewriter;
    private final AiMetrics aiMetrics;

    @Tool(description = "检索 StarPivot 内部知识库（产品功能、配置、操作步骤、政策说明）。用户问平台怎么用、如何配置、某功能在哪时必须先调用本工具，再根据结果作答。")
    public String searchKnowledgeBase(
            @ToolParam(description = "完整、独立的检索问题，不要使用“这个/那个”等指代") String query,
            ToolContext toolContext) {
        aiMetrics.recordAgentTool("search_knowledge_base");
        String conversationId = contextString(toolContext, "conversationId");
        agentToolStatusNotifier.notify(conversationId, "正在检索知识库…");
        if (!StringUtils.hasText(query)) {
            return "检索问题为空，请改写为完整问题后再试。";
        }
        if (!canQuery(toolContext)) {
            return "当前用户没有知识库查询权限，请直接基于通用能力回答，不要编造内部功能。";
        }
        if (!ragEnabled(toolContext)) {
            return "知识库检索未启用。";
        }

        int topK = resolveTopK(toolContext);
        try {
            String retrievalQuery = ragConversationalRewriter.rewrite(conversationId, query.trim(), true);
            RagRetrievalResult result = aiKnowledgeRetrievalService.retrieve(retrievalQuery, topK);
            agentSourceCollector.addAll(conversationId, result.getSources());
            if (!StringUtils.hasText(result.getContext())) {
                agentToolStatusNotifier.notify(conversationId, "知识库暂无匹配，改用通用能力…");
                return "知识库未检索到相关资料。请明确告知用户暂未找到内部说明，不要编造。";
            }
            agentToolStatusNotifier.notify(conversationId, "已检索知识库，正在写回答…");
            return result.getContext();
        } catch (RuntimeException ex) {
            log.warn("[Agent] knowledge search failed: {}", ex.getMessage());
            agentToolStatusNotifier.notify(conversationId, "知识库检索失败，改用通用能力…");
            return "知识库检索失败，请基于通用能力谨慎回答，不要编造内部功能。";
        }
    }

    private boolean canQuery(ToolContext toolContext) {
        Object flag = contextValue(toolContext, "canQueryKnowledge");
        if (flag instanceof Boolean bool) {
            return bool;
        }
        if (flag != null) {
            return Boolean.parseBoolean(String.valueOf(flag));
        }
        return false;
    }

    private boolean ragEnabled(ToolContext toolContext) {
        Object flag = contextValue(toolContext, "ragEnabled");
        if (flag instanceof Boolean bool) {
            return bool;
        }
        if (flag != null) {
            return Boolean.parseBoolean(String.valueOf(flag));
        }
        return aiRuntimeConfigService.current().isRagEnabled();
    }

    private int resolveTopK(ToolContext toolContext) {
        Object value = contextValue(toolContext, "ragTopK");
        if (value instanceof Number number) {
            return Math.max(number.intValue(), 1);
        }
        if (value != null) {
            try {
                return Math.max(Integer.parseInt(String.valueOf(value)), 1);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        int configured = aiRuntimeConfigService.current().getRagTopK();
        return configured > 0 ? configured : 8;
    }

    private static String contextString(ToolContext toolContext, String key) {
        Object value = contextValue(toolContext, key);
        return value != null ? String.valueOf(value) : null;
    }

    private static Object contextValue(ToolContext toolContext, String key) {
        if (toolContext == null || toolContext.getContext() == null) {
            return null;
        }
        return toolContext.getContext().get(key);
    }
}
