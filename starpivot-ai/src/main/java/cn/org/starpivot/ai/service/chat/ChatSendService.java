package cn.org.starpivot.ai.service.chat;

import cn.org.starpivot.ai.config.AiRuntimeSnapshot;
import cn.org.starpivot.ai.domain.dto.ChatSendDto;
import cn.org.starpivot.ai.domain.vo.ChatReplyVo;
import cn.org.starpivot.ai.domain.vo.RagRetrievalResult;
import cn.org.starpivot.ai.metrics.AiMetrics;
import cn.org.starpivot.ai.service.AiChatRateLimitService;
import cn.org.starpivot.ai.service.AiRuntimeConfigService;
import cn.org.starpivot.ai.service.AiUsageLogService;
import cn.org.starpivot.ai.service.AiUsageLogService.UsageContext;
import cn.org.starpivot.common.security.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatSendService {

    private final ChatHealthService chatHealthService;
    private final ChatSessionService chatSessionService;
    private final ChatExecutionPlanner chatExecutionPlanner;
    private final ChatPromptAssembler chatPromptAssembler;
    private final AiRuntimeConfigService aiRuntimeConfigService;
    private final AiChatRateLimitService aiChatRateLimitService;
    private final AiUsageLogService aiUsageLogService;
    private final AiMetrics aiMetrics;

    public ChatReplyVo send(ChatSendDto dto) {
        chatHealthService.assertConfigured();
        aiChatRateLimitService.checkChatRequest();

        String conversationId = chatSessionService.resolveConversationId(dto.getConversationId());
        chatSessionService.prepareConversation(dto, conversationId);
        chatSessionService.touchSession(conversationId, dto.getMessage());

        AiRuntimeSnapshot runtime = aiRuntimeConfigService.current();
        ChatExecutionPlan plan = chatExecutionPlanner.plan(dto, runtime);
        long start = System.currentTimeMillis();
        UsageContext usageContext = buildUsageContext(conversationId, plan.model(), "SEND", dto.getMessage(), 0);
        RagRetrievalResult ragResult = chatPromptAssembler.retrieve(runtime, dto.getMessage(), plan.useRag());

        try {
            ChatResponse response = chatPromptAssembler
                    .buildPrompt(dto, conversationId, runtime, ragResult, plan)
                    .call()
                    .chatResponse();
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
            return ChatReplyVo.builder()
                    .conversationId(conversationId)
                    .reply(reply)
                    .sources(ragResult.getSources() != null ? ragResult.getSources() : List.of())
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
        }
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
