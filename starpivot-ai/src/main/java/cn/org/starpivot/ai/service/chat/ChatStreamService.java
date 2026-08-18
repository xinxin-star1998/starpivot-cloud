package cn.org.starpivot.ai.service.chat;

import cn.org.starpivot.ai.agent.AgentSourceCollector;
import cn.org.starpivot.ai.agent.AgentToolStatusNotifier;
import cn.org.starpivot.ai.config.AiRuntimeSnapshot;
import cn.org.starpivot.ai.domain.dto.ChatSendDto;
import cn.org.starpivot.ai.domain.vo.RagRetrievalResult;
import cn.org.starpivot.ai.domain.vo.RagSourceVo;
import cn.org.starpivot.ai.memory.ConversationSummaryService;
import cn.org.starpivot.ai.memory.MysqlChatMemoryRepository;
import cn.org.starpivot.ai.metrics.AiMetrics;
import cn.org.starpivot.ai.rag.RagConversationalRewriter;
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
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.util.List;
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
    private final RagConversationalRewriter ragConversationalRewriter;
    private final AgentSourceCollector agentSourceCollector;
    private final AgentToolStatusNotifier agentToolStatusNotifier;
    private final ConversationSummaryService conversationSummaryService;
    private final MysqlChatMemoryRepository chatMemoryRepository;
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
            RagConversationalRewriter ragConversationalRewriter,
            AgentSourceCollector agentSourceCollector,
            AgentToolStatusNotifier agentToolStatusNotifier,
            ConversationSummaryService conversationSummaryService,
            MysqlChatMemoryRepository chatMemoryRepository,
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
        this.ragConversationalRewriter = ragConversationalRewriter;
        this.agentSourceCollector = agentSourceCollector;
        this.agentToolStatusNotifier = agentToolStatusNotifier;
        this.conversationSummaryService = conversationSummaryService;
        this.chatMemoryRepository = chatMemoryRepository;
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
        agentSourceCollector.begin(conversationId);

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
            chatSseEmitterSupport.sendStatusEvent(emitter, "routing", "正在理解你的问题…");

            AiRuntimeSnapshot runtime = aiRuntimeConfigService.current();
            ChatExecutionPlan plan = chatExecutionPlanner.plan(dto, runtime, conversationId);
            UsageContext usageContext = buildUsageContext(conversationId, plan.model(), "STREAM", dto.getMessage(), 0);

            if (plan.useRag() || plan.useAgent()) {
                chatSseEmitterSupport.sendStatusEvent(emitter, "rewriting", "正在整理检索关键词…");
            }
            String retrievalQuery = ragConversationalRewriter.rewrite(
                    conversationId, dto.getMessage(), plan.useRag() || plan.useAgent());
            if (plan.useRag()) {
                chatSseEmitterSupport.sendStatusEvent(emitter, "retrieving", "正在查找相关资料…");
            }
            RagRetrievalResult ragResult = chatPromptAssembler.retrieve(runtime, retrievalQuery, plan.useRag());
            String rewrittenForMeta =
                    (plan.useRag() || plan.useAgent()) && !retrievalQuery.equals(dto.getMessage())
                            ? retrievalQuery
                            : null;
            chatSseEmitterSupport.sendMetaEvent(emitter, conversationId, ragResult, plan, rewrittenForMeta);

            if (plan.useAgent()
                    && !(StringUtils.hasText(ragResult.getContext()) && !ragResult.isDegraded())) {
                agentToolStatusNotifier.listen(
                        conversationId,
                        message -> chatSseEmitterSupport.sendStatusEvent(emitter, "tooling", message));
                chatSseEmitterSupport.sendStatusEvent(emitter, "tooling", "正在使用工具…");
            } else {
                chatSseEmitterSupport.sendStatusEvent(emitter, "generating", "正在写回答…");
            }

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
                                agentToolStatusNotifier.clear(conversationId);
                                agentSourceCollector.drain(conversationId);
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
                                agentToolStatusNotifier.clear(conversationId);
                                List<RagSourceVo> extraSources = agentSourceCollector.drain(conversationId);
                                List<RagSourceVo> mergedSources = AgentSourceCollector.merge(
                                        ragResult.getSources(), extraSources);
                                if (!mergedSources.isEmpty()) {
                                    RagRetrievalResult merged = RagRetrievalResult.builder()
                                            .context(ragResult.getContext())
                                            .sources(mergedSources)
                                            .degraded(ragResult.isDegraded())
                                            .build();
                                    chatSseEmitterSupport.sendMetaEvent(emitter, conversationId, merged, plan);
                                    chatMemoryRepository.attachSourcesToLastAssistant(conversationId, mergedSources);
                                }
                                chatSseEmitterSupport.completeStream(emitter);
                                conversationSummaryService.refreshAsync(conversationId);
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
            agentToolStatusNotifier.clear(conversationId);
            agentSourceCollector.drain(conversationId);
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
