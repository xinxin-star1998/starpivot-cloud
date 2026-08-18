package cn.org.starpivot.ai.service.chat;

import cn.org.starpivot.ai.agent.AgentSourceCollector;
import cn.org.starpivot.ai.config.AiRuntimeSnapshot;
import cn.org.starpivot.ai.domain.dto.ChatSendDto;
import cn.org.starpivot.ai.domain.vo.ChatReplyVo;
import cn.org.starpivot.ai.domain.vo.RagRetrievalResult;
import cn.org.starpivot.ai.memory.ConversationSummaryService;
import cn.org.starpivot.ai.memory.MysqlChatMemoryRepository;
import cn.org.starpivot.ai.metrics.AiMetrics;
import cn.org.starpivot.ai.provider.AiModelClientFactory;
import cn.org.starpivot.ai.rag.RagConversationalRewriter;
import cn.org.starpivot.ai.service.AiChatRateLimitService;
import cn.org.starpivot.ai.service.AiRuntimeConfigService;
import cn.org.starpivot.ai.service.AiUsageLogService;
import cn.org.starpivot.ai.service.AiUsageLogService.UsageContext;
import cn.org.starpivot.common.security.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSendService {

    private final ChatHealthService chatHealthService;
    private final ChatSessionService chatSessionService;
    private final ChatExecutionPlanner chatExecutionPlanner;
    private final ChatPromptAssembler chatPromptAssembler;
    private final RagConversationalRewriter ragConversationalRewriter;
    private final AgentSourceCollector agentSourceCollector;
    private final ConversationSummaryService conversationSummaryService;
    private final MysqlChatMemoryRepository chatMemoryRepository;
    private final AiRuntimeConfigService aiRuntimeConfigService;
    private final AiChatRateLimitService aiChatRateLimitService;
    private final AiUsageLogService aiUsageLogService;
    private final AiMetrics aiMetrics;
    private final AiModelClientFactory aiModelClientFactory;

    public ChatReplyVo send(ChatSendDto dto) {
        chatHealthService.assertConfigured();
        aiChatRateLimitService.checkChatRequest();

        String conversationId = chatSessionService.resolveConversationId(dto.getConversationId());
        chatSessionService.prepareConversation(dto, conversationId);
        chatSessionService.touchSession(conversationId, dto.getMessage());
        agentSourceCollector.begin(conversationId);

        AiRuntimeSnapshot runtime = aiRuntimeConfigService.current();
        ChatExecutionPlan plan = chatExecutionPlanner.plan(dto, runtime, conversationId);
        long start = System.currentTimeMillis();
        UsageContext usageContext = buildUsageContext(conversationId, plan.model(), "SEND", dto.getMessage(), 0);
        String retrievalQuery = ragConversationalRewriter.rewrite(
                conversationId, dto.getMessage(), plan.useRag() || plan.useAgent());
        RagRetrievalResult ragResult = chatPromptAssembler.retrieve(runtime, retrievalQuery, plan.useRag());

        try {
            ChatResponse response;
            try {
                response = chatPromptAssembler
                        .buildPrompt(dto, conversationId, runtime, ragResult, plan)
                        .call()
                        .chatResponse();
            } catch (RuntimeException primaryEx) {
                ChatExecutionPlan failoverPlan = tryFailoverPlan(plan);
                if (failoverPlan == null || !AiModelClientFactory.isRetriableProviderError(primaryEx)) {
                    throw primaryEx;
                }
                log.warn("[AI Provider] send failover after {}: {}", primaryEx.getMessage(), failoverPlan.model());
                plan = failoverPlan;
                response = chatPromptAssembler
                        .buildPrompt(dto, conversationId, runtime, ragResult, plan)
                        .call()
                        .chatResponse();
            }
            String reply = response.getResult() != null && response.getResult().getOutput() != null
                    ? response.getResult().getOutput().getText()
                    : "";
            long latency = System.currentTimeMillis() - start;
            aiUsageLogService.recordSuccess(
                    response,
                    new UsageContext(
                            usageContext.userId(),
                            usageContext.conversationId(),
                            plan.model(),
                            usageContext.requestType(),
                            latency,
                            usageContext.userMessageLength(),
                            reply != null ? reply.length() : 0));
            aiMetrics.recordChat("SEND", plan.model(), true, latency);
            conversationSummaryService.refreshAsync(conversationId);
            var sources = AgentSourceCollector.merge(
                    ragResult.getSources(),
                    agentSourceCollector.drain(conversationId));
            chatMemoryRepository.attachSourcesToLastAssistant(conversationId, sources);
            return ChatReplyVo.builder()
                    .conversationId(conversationId)
                    .reply(reply)
                    .sources(sources)
                    .intent(plan.intent() != null ? plan.intent().name() : null)
                    .rewrittenQuery((plan.useRag() || plan.useAgent()) ? retrievalQuery : null)
                    .build();
        } catch (RuntimeException ex) {
            long latency = System.currentTimeMillis() - start;
            aiUsageLogService.recordFailure(
                    new UsageContext(
                            usageContext.userId(),
                            usageContext.conversationId(),
                            plan.model(),
                            usageContext.requestType(),
                            latency,
                            usageContext.userMessageLength(),
                            0),
                    ex.getMessage());
            aiMetrics.recordChat("SEND", plan.model(), false, latency);
            throw ex;
        } finally {
            agentSourceCollector.drain(conversationId);
        }
    }

    private ChatExecutionPlan tryFailoverPlan(ChatExecutionPlan plan) {
        String failoverRef = aiModelClientFactory.failoverModelRef(plan.model());
        if (!StringUtils.hasText(failoverRef) || failoverRef.equals(plan.model())) {
            return null;
        }
        return ChatExecutionPlan.builder()
                .intent(plan.intent())
                .promptScene(plan.promptScene())
                .model(failoverRef)
                .useRag(plan.useRag())
                .useAgent(plan.useAgent())
                .autoScene(plan.autoScene())
                .autoModel(plan.autoModel())
                .build();
    }

    private UsageContext buildUsageContext(
            String conversationId, String model, String requestType, String userMessage, int completionLength) {
        return new UsageContext(
                SecurityContextUtils.getUserId(),
                conversationId,
                model,
                requestType,
                0L,
                userMessage != null ? userMessage.length() : 0,
                completionLength);
    }
}
