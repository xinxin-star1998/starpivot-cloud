package cn.org.starpivot.ai.service.chat;

import cn.org.starpivot.ai.config.AiRuntimeSnapshot;
import cn.org.starpivot.ai.domain.dto.ChatSendDto;
import cn.org.starpivot.ai.domain.vo.RagRetrievalResult;
import cn.org.starpivot.ai.metrics.AiMetrics;
import cn.org.starpivot.ai.service.AiChatRateLimitService;
import cn.org.starpivot.ai.service.AiRuntimeConfigService;
import cn.org.starpivot.ai.service.AiUsageLogService;
import cn.org.starpivot.ai.service.AiUsageLogService.UsageContext;
import cn.org.starpivot.common.security.SecurityContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class ChatStreamService {

    private static final long STREAM_TIMEOUT_MS = 120_000L;

    private final ChatHealthService chatHealthService;
    private final ChatSessionService chatSessionService;
    private final ChatExecutionPlanner chatExecutionPlanner;
    private final ChatPromptAssembler chatPromptAssembler;
    private final ChatSseEmitterSupport chatSseEmitterSupport;
    private final AiRuntimeConfigService aiRuntimeConfigService;
    private final AiChatRateLimitService aiChatRateLimitService;
    private final AiUsageLogService aiUsageLogService;
    private final AiMetrics aiMetrics;
    private final Executor chatStreamExecutor;

    public ChatStreamService(
            ChatHealthService chatHealthService,
            ChatSessionService chatSessionService,
            ChatExecutionPlanner chatExecutionPlanner,
            ChatPromptAssembler chatPromptAssembler,
            ChatSseEmitterSupport chatSseEmitterSupport,
            AiRuntimeConfigService aiRuntimeConfigService,
            AiChatRateLimitService aiChatRateLimitService,
            AiUsageLogService aiUsageLogService,
            AiMetrics aiMetrics,
            @Qualifier("chatStreamExecutor") Executor chatStreamExecutor) {
        this.chatHealthService = chatHealthService;
        this.chatSessionService = chatSessionService;
        this.chatExecutionPlanner = chatExecutionPlanner;
        this.chatPromptAssembler = chatPromptAssembler;
        this.chatSseEmitterSupport = chatSseEmitterSupport;
        this.aiRuntimeConfigService = aiRuntimeConfigService;
        this.aiChatRateLimitService = aiChatRateLimitService;
        this.aiUsageLogService = aiUsageLogService;
        this.aiMetrics = aiMetrics;
        this.chatStreamExecutor = chatStreamExecutor;
    }

    public SseEmitter stream(ChatSendDto dto) {
        chatHealthService.assertConfigured();
        aiChatRateLimitService.checkChatRequest();

        String conversationId = chatSessionService.resolveConversationId(dto.getConversationId());
        chatSessionService.prepareConversation(dto, conversationId);
        chatSessionService.touchSession(conversationId, dto.getMessage());

        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MS);
        AtomicReference<Disposable> subscription = new AtomicReference<>();

        Runnable disposeSubscription = () -> {
            Disposable disposable = subscription.get();
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        };

        emitter.onTimeout(() -> {
            disposeSubscription.run();
            emitter.complete();
        });
        emitter.onCompletion(disposeSubscription);
        emitter.onError(ex -> {
            log.warn("AI chat stream connection error", ex);
            disposeSubscription.run();
        });

        chatStreamExecutor.execute(() -> runStream(dto, conversationId, emitter, subscription));
        return emitter;
    }

    private void runStream(
            ChatSendDto dto,
            String conversationId,
            SseEmitter emitter,
            AtomicReference<Disposable> subscription) {
        long start = System.currentTimeMillis();
        SecurityContext securityContext = copySecurityContext();
        try {
            chatSseEmitterSupport.sendStatusEvent(emitter, "routing", "正在分析意图与路由…");

            AiRuntimeSnapshot runtime = aiRuntimeConfigService.current();
            ChatExecutionPlan plan = chatExecutionPlanner.plan(dto, runtime);
            UsageContext usageContext = buildUsageContext(conversationId, plan.model(), "STREAM", dto.getMessage(), 0);

            if (plan.useRag()) {
                chatSseEmitterSupport.sendStatusEvent(emitter, "retrieving", "正在检索知识库…");
            }
            RagRetrievalResult ragResult = chatPromptAssembler.retrieve(runtime, dto.getMessage(), plan.useRag());
            chatSseEmitterSupport.sendMetaEvent(emitter, conversationId, ragResult, plan);

            chatSseEmitterSupport.sendStatusEvent(emitter, "generating", "正在生成回答…");

            AtomicReference<ChatResponse> lastResponse = new AtomicReference<>();
            StringBuilder completion = new StringBuilder();

            Disposable disposable = chatPromptAssembler.buildPrompt(dto, conversationId, runtime, ragResult, plan)
                    .stream()
                    .chatResponse()
                    .subscribe(
                            response -> runWithSecurityContext(securityContext, () -> {
                                lastResponse.set(response);
                                if (response.getResult() != null
                                        && response.getResult().getOutput() != null
                                        && response.getResult().getOutput().getText() != null) {
                                    String chunk = response.getResult().getOutput().getText();
                                    completion.append(chunk);
                                    chatSseEmitterSupport.sendDeltaEvent(emitter, chunk);
                                }
                            }),
                            error -> runWithSecurityContext(securityContext, () -> {
                                long latency = System.currentTimeMillis() - start;
                                aiUsageLogService.recordFailure(
                                        new UsageContext(
                                                usageContext.userId(),
                                                usageContext.conversationId(),
                                                plan.model(),
                                                usageContext.requestType(),
                                                latency,
                                                usageContext.userMessageLength(),
                                                completion.length()),
                                        error.getMessage());
                                aiMetrics.recordChat("STREAM", plan.model(), false, latency);
                                chatSseEmitterSupport.handleStreamError(emitter, error);
                            }),
                            () -> runWithSecurityContext(securityContext, () -> {
                                chatSseEmitterSupport.completeStream(emitter);
                                long latency = System.currentTimeMillis() - start;
                                aiUsageLogService.recordSuccess(
                                        lastResponse.get(),
                                        new UsageContext(
                                                usageContext.userId(),
                                                usageContext.conversationId(),
                                                plan.model(),
                                                usageContext.requestType(),
                                                latency,
                                                usageContext.userMessageLength(),
                                                completion.length()));
                                aiMetrics.recordChat("STREAM", plan.model(), true, latency);
                            }));
            subscription.set(disposable);
        } catch (Exception ex) {
            chatSseEmitterSupport.handleStreamError(emitter, ex);
        }
    }

    private SecurityContext copySecurityContext() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        context.setAuthentication(authentication);
        return context;
    }

    private void runWithSecurityContext(SecurityContext context, Runnable task) {
        SecurityContext previous = SecurityContextHolder.getContext();
        try {
            SecurityContextHolder.setContext(context);
            task.run();
        } finally {
            SecurityContextHolder.setContext(previous);
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
