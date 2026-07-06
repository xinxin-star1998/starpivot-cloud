package cn.org.starpivot.ai.service;

import cn.org.starpivot.ai.config.AiRuntimeSnapshot;
import cn.org.starpivot.ai.domain.dto.ChatSendDto;
import cn.org.starpivot.ai.domain.dto.SessionRenameDto;
import cn.org.starpivot.ai.domain.entity.AiChatMessage;
import cn.org.starpivot.ai.domain.vo.*;
import cn.org.starpivot.ai.memory.MysqlChatMemoryRepository;
import cn.org.starpivot.ai.memory.MysqlChatSessionRepository;
import cn.org.starpivot.ai.service.AiUsageLogService.UsageContext;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.security.SecurityContextUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final long STREAM_TIMEOUT_MS = 120_000L;

    private final ChatClient chatClient;
    private final MessageWindowChatMemory chatMemory;
    private final MysqlChatMemoryRepository chatMemoryRepository;
    private final MysqlChatSessionRepository chatSessionRepository;
    private final AiRuntimeConfigService aiRuntimeConfigService;
    private final AiKnowledgeRetrievalService aiKnowledgeRetrievalService;
    private final AiUsageLogService aiUsageLogService;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    public ChatReplyVo send(ChatSendDto dto) {
        assertConfigured();

        String conversationId = resolveConversationId(dto.getConversationId());
        prepareConversation(dto, conversationId);
        touchSession(conversationId, dto.getMessage());
        AiRuntimeSnapshot runtime = aiRuntimeConfigService.current();
        String model = resolveModel(dto.getModel(), runtime);
        long start = System.currentTimeMillis();
        UsageContext usageContext = buildUsageContext(conversationId, model, "SEND", dto.getMessage(), 0);
        RagRetrievalResult ragResult = retrieveIfEnabled(runtime, dto.getMessage());
        try {
            ChatResponse response = buildPrompt(dto, conversationId, runtime, ragResult).call().chatResponse();
            String reply = response.getResult() != null && response.getResult().getOutput() != null
                    ? response.getResult().getOutput().getText()
                    : "";
            usageContext = buildUsageContext(
                    conversationId, model, "SEND", dto.getMessage(), reply != null ? reply.length() : 0);
            aiUsageLogService.recordSuccess(
                    response,
                    new UsageContext(
                            usageContext.userId(),
                            usageContext.conversationId(),
                            usageContext.model(),
                            usageContext.requestType(),
                            System.currentTimeMillis() - start,
                            usageContext.userMessageLength(),
                            usageContext.completionLength()));
            return ChatReplyVo.builder()
                    .conversationId(conversationId)
                    .reply(reply)
                    .sources(ragResult.getSources() != null ? ragResult.getSources() : List.of())
                    .build();
        } catch (RuntimeException ex) {
            aiUsageLogService.recordFailure(
                    new UsageContext(
                            usageContext.userId(),
                            usageContext.conversationId(),
                            usageContext.model(),
                            usageContext.requestType(),
                            System.currentTimeMillis() - start,
                            usageContext.userMessageLength(),
                            0),
                    ex.getMessage());
            throw ex;
        }
    }

    public SseEmitter stream(ChatSendDto dto) {
        assertConfigured();

        String conversationId = resolveConversationId(dto.getConversationId());
        prepareConversation(dto, conversationId);
        touchSession(conversationId, dto.getMessage());
        AiRuntimeSnapshot runtime = aiRuntimeConfigService.current();
        String model = resolveModel(dto.getModel(), runtime);
        long start = System.currentTimeMillis();
        UsageContext usageContext = buildUsageContext(conversationId, model, "STREAM", dto.getMessage(), 0);
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MS);

        RagRetrievalResult ragResult = retrieveIfEnabled(runtime, dto.getMessage());
        sendMetaEvent(emitter, conversationId, ragResult.getSources() != null ? ragResult.getSources() : List.of());

        AtomicReference<ChatResponse> lastResponse = new AtomicReference<>();
        StringBuilder completion = new StringBuilder();

        buildPrompt(dto, conversationId, runtime, ragResult)
                .stream()
                .chatResponse()
                .subscribe(
                        response -> {
                            lastResponse.set(response);
                            if (response.getResult() != null
                                    && response.getResult().getOutput() != null
                                    && response.getResult().getOutput().getText() != null) {
                                String chunk = response.getResult().getOutput().getText();
                                completion.append(chunk);
                                sendDeltaEvent(emitter, chunk);
                            }
                        },
                        error -> {
                            aiUsageLogService.recordFailure(
                                    new UsageContext(
                                            usageContext.userId(),
                                            usageContext.conversationId(),
                                            usageContext.model(),
                                            usageContext.requestType(),
                                            System.currentTimeMillis() - start,
                                            usageContext.userMessageLength(),
                                            completion.length()),
                                    error.getMessage());
                            handleStreamError(emitter, error);
                        },
                        () -> {
                            completeStream(emitter);
                            aiUsageLogService.recordSuccess(
                                    lastResponse.get(),
                                    new UsageContext(
                                            usageContext.userId(),
                                            usageContext.conversationId(),
                                            usageContext.model(),
                                            usageContext.requestType(),
                                            System.currentTimeMillis() - start,
                                            usageContext.userMessageLength(),
                                            completion.length()));
                        });

        emitter.onTimeout(emitter::complete);
        emitter.onError(ex -> log.warn("AI chat stream connection error", ex));
        return emitter;
    }

    public AiHealthVo health() {
        AiRuntimeSnapshot runtime = aiRuntimeConfigService.current();
        String botAvatar = StringUtils.hasText(runtime.getBotAvatar())
                ? runtime.getBotAvatar().trim()
                : null;
        String botName = StringUtils.hasText(runtime.getBotName())
                ? runtime.getBotName().trim()
                : "AI 助手";
        AiHealthVo.AiHealthVoBuilder builder = AiHealthVo.builder()
                .botAvatar(botAvatar)
                .botName(botName)
                .welcomeMessage(runtime.resolvedWelcomeMessage())
                .models(runtime.getModels())
                .defaultModel(runtime.getDefaultModel())
                .defaultTemperature(runtime.getDefaultTemperature())
                .maxMemoryMessages(runtime.getMaxMemoryMessages());

        if (!StringUtils.hasText(apiKey)) {
            return builder
                    .online(false)
                    .message("未配置 AI API Key，请在 Nacos 中设置 spring.ai.openai.api-key")
                    .build();
        }
        return builder.online(true).message("在线").build();
    }

    public void clearHistory(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            throw new BizException("conversationId 不能为空");
        }
        String resolvedConversationId = resolveConversationId(conversationId);
        chatMemoryRepository.deleteByConversationId(resolvedConversationId);
    }

    public ChatSessionVo createSession() {
        Long userId = requireUserId();
        String conversationId = buildConversationId(userId);
        chatSessionRepository.upsertSession(userId, conversationId, "新对话");
        return ChatSessionVo.builder()
                .conversationId(conversationId)
                .title("新对话")
                .updatedAt(System.currentTimeMillis())
                .build();
    }

    public List<ChatSessionVo> listSessions() {
        return chatSessionRepository.listSessions(requireUserId());
    }

    public List<ChatHistoryMessageVo> listMessages(String conversationId) {
        String resolvedConversationId = resolveConversationId(conversationId);
        return chatMemoryRepository.listRawMessages(resolvedConversationId).stream()
                .map(this::toHistoryVo)
                .collect(Collectors.toList());
    }

    public void deleteSession(String conversationId) {
        String resolvedConversationId = resolveConversationId(conversationId);
        chatMemoryRepository.deleteByConversationId(resolvedConversationId);
        chatSessionRepository.removeSession(requireUserId(), resolvedConversationId);
    }

    public ChatSessionVo renameSession(SessionRenameDto dto) {
        String resolvedConversationId = resolveConversationId(dto.getConversationId());
        Long userId = requireUserId();
        chatSessionRepository.renameSession(userId, resolvedConversationId, dto.getTitle().trim());
        return ChatSessionVo.builder()
                .conversationId(resolvedConversationId)
                .title(dto.getTitle().trim())
                .updatedAt(System.currentTimeMillis())
                .build();
    }

    private ChatHistoryMessageVo toHistoryVo(AiChatMessage message) {
        Long createTime = message.getCreateTime() != null
                ? message.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                : null;
        return ChatHistoryMessageVo.builder()
                .role(message.getRole())
                .content(message.getContent())
                .createTime(createTime)
                .build();
    }

    private ChatClient.ChatClientRequestSpec buildPrompt(
            ChatSendDto dto, String conversationId, AiRuntimeSnapshot runtime, RagRetrievalResult ragResult) {
        String model = resolveModel(dto.getModel(), runtime);
        Double temperature = dto.getTemperature() != null
                ? dto.getTemperature()
                : runtime.getDefaultTemperature();

        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder();
        if (StringUtils.hasText(model)) {
            optionsBuilder.model(model);
        }
        if (temperature != null) {
            optionsBuilder.temperature(temperature);
        }

        return chatClient
                .prompt()
                .options(optionsBuilder.build())
                .system(buildSystemPrompt(runtime, ragResult))
                .user(dto.getMessage())
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId(conversationId)
                        .build());
    }

    private RagRetrievalResult retrieveIfEnabled(AiRuntimeSnapshot runtime, String userMessage) {
        if (!runtime.isRagEnabled() || !StringUtils.hasText(userMessage)) {
            return RagRetrievalResult.builder().context("").sources(List.of()).build();
        }
        try {
            return aiKnowledgeRetrievalService.retrieve(userMessage, runtime.getRagTopK());
        } catch (RuntimeException ex) {
            log.warn("RAG retrieval failed, continue without knowledge context: {}", ex.getMessage());
            return RagRetrievalResult.builder().context("").sources(List.of()).build();
        }
    }

    private String buildSystemPrompt(AiRuntimeSnapshot runtime, RagRetrievalResult ragResult) {
        String systemPrompt = runtime.getSystemPrompt();
        if (!StringUtils.hasText(ragResult.getContext())) {
            return systemPrompt;
        }
        return systemPrompt
                + "\n\n以下是可能相关的参考资料，请优先依据资料回答；若资料不足以回答，请结合通用知识并诚实说明：\n"
                + ragResult.getContext();
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

    private String resolveModel(String requestedModel, AiRuntimeSnapshot runtime) {
        if (!StringUtils.hasText(requestedModel)) {
            return runtime.getDefaultModel();
        }
        String model = requestedModel.trim();
        if (!runtime.isModelAllowed(model)) {
            throw new BizException("不支持的模型：" + model);
        }
        return model;
    }

    private void touchSession(String conversationId, String userMessage) {
        Long userId = SecurityContextUtils.getUserId();
        if (userId == null) {
            return;
        }
        chatSessionRepository.touchSession(userId, conversationId, userMessage);
    }

    private Long requireUserId() {
        Long userId = SecurityContextUtils.getUserId();
        if (userId == null) {
            throw new BizException("请先登录后再使用 AI 对话");
        }
        return userId;
    }

    private String buildConversationId(Long userId) {
        return "user-" + userId + ":" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private void sendMetaEvent(SseEmitter emitter, String conversationId, List<RagSourceVo> sources) {
        try {
            Map<String, Object> payload = new java.util.LinkedHashMap<>();
            payload.put("conversationId", conversationId);
            if (sources != null && !sources.isEmpty()) {
                payload.put("sources", sources);
            }
            emitter.send(SseEmitter.event().name("meta").data(objectMapper.writeValueAsString(payload), MediaType.APPLICATION_JSON));
        } catch (IOException ex) {
            emitter.completeWithError(ex);
        }
    }

    private void sendDeltaEvent(SseEmitter emitter, String chunk) {
        try {
            emitter.send(SseEmitter.event().name("delta").data(chunk));
        } catch (IOException ex) {
            emitter.completeWithError(ex);
        }
    }

    private void handleStreamError(SseEmitter emitter, Throwable error) {
        log.warn("AI chat stream failed", error);
        try {
            String message = error.getMessage() != null ? error.getMessage() : "AI 生成失败";
            emitter.send(SseEmitter.event().name("error").data(message));
        } catch (IOException ignored) {
            // ignore secondary failure
        }
        emitter.completeWithError(error);
    }

    private void completeStream(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("done").data("[DONE]"));
        } catch (IOException ex) {
            emitter.completeWithError(ex);
            return;
        }
        emitter.complete();
    }

    private void prepareConversation(ChatSendDto dto, String conversationId) {
        if (Boolean.TRUE.equals(dto.getRegenerate())) {
            popLastExchange(conversationId);
        }
    }

    private void popLastExchange(String conversationId) {
        List<Message> messages = new ArrayList<>(chatMemoryRepository.findByConversationId(conversationId));
        if (messages.isEmpty()) {
            return;
        }

        Message last = messages.get(messages.size() - 1);
        if (last instanceof AssistantMessage) {
            messages.remove(messages.size() - 1);
        }
        if (!messages.isEmpty()) {
            Message previous = messages.get(messages.size() - 1);
            if (previous instanceof UserMessage) {
                messages.remove(messages.size() - 1);
            }
        }

        if (messages.isEmpty()) {
            chatMemoryRepository.deleteByConversationId(conversationId);
        } else {
            chatMemoryRepository.saveAll(conversationId, messages);
        }
    }

    private void assertConfigured() {
        if (!StringUtils.hasText(apiKey)) {
            throw new BizException("AI 服务未配置，请联系管理员设置 API Key");
        }
    }

    private String resolveConversationId(String conversationId) {
        Long userId = SecurityContextUtils.getUserId();
        if (userId == null) {
            throw new BizException("请先登录后再使用 AI 对话");
        }
        if (StringUtils.hasText(conversationId)) {
            String resolved = conversationId.trim();
            if (!resolved.startsWith("user-" + userId + ":")) {
                throw new BizException("无权访问该会话");
            }
            if (chatSessionRepository.findActiveSession(resolved) == null) {
                chatSessionRepository.upsertSession(userId, resolved, "新对话");
            } else {
                chatSessionRepository.assertSessionOwner(resolved, userId);
            }
            return resolved;
        }

        String newConversationId = buildConversationId(userId);
        chatSessionRepository.upsertSession(userId, newConversationId, "新对话");
        return newConversationId;
    }
}
