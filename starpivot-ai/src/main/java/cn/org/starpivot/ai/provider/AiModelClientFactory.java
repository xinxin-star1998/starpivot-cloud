package cn.org.starpivot.ai.provider;

import cn.org.starpivot.ai.domain.entity.AiProvider;
import cn.org.starpivot.ai.service.AiProviderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AiModelClientFactory {

    private final ObjectProvider<AiProviderService> aiProviderService;
    private final ObjectProvider<ChatClient> fallbackChatClient;
    private final ObjectProvider<EmbeddingModel> fallbackEmbeddingModel;
    private final ConcurrentHashMap<String, ChatClient> chatCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, EmbeddingModel> embeddingCache = new ConcurrentHashMap<>();

    public AiModelClientFactory(
            ObjectProvider<AiProviderService> aiProviderService,
            ObjectProvider<ChatClient> fallbackChatClient,
            ObjectProvider<EmbeddingModel> fallbackEmbeddingModel) {
        this.aiProviderService = aiProviderService;
        this.fallbackChatClient = fallbackChatClient;
        this.fallbackEmbeddingModel = fallbackEmbeddingModel;
    }

    public void evict() {
        chatCache.clear();
        embeddingCache.clear();
    }

    @EventListener
    public void onProviderChanged(AiProviderChangedEvent event) {
        evict();
    }

    public boolean hasChatCredential() {
        AiProviderService service = aiProviderService.getIfAvailable();
        return service != null && service.findDefaultChatProvider() != null;
    }

    public ChatClient chatClient() {
        return chatClient(null);
    }

    public ChatClient chatClient(String model) {
        AiProvider provider = providers().findChatProvider(model);
        if (provider != null) {
            String resolvedModel = providers().resolveChatModel(provider, model);
            return chatCache.computeIfAbsent(
                    chatKey(provider, resolvedModel), key -> createChatClient(provider, resolvedModel));
        }
        ChatClient fallback = fallbackChatClient.getIfAvailable();
        if (fallback != null) {
            log.warn("[AI Provider] no enabled chat provider, fallback to YAML client model={}", model);
            return fallback;
        }
        throw new IllegalStateException("未配置可用的对话供应商，请在「AI 中心 → 模型供应商」中填写 API Key");
    }

    /**
     * 主供应商失败时的备用对话客户端；无可用备用时返回 null。
     */
    public ChatClient failoverChatClient(String model) {
        AiProvider primary = providers().findChatProvider(model);
        Long excludeId = primary != null ? primary.getProviderId() : null;
        AiProvider failover = providers().findFailoverChatProvider(excludeId);
        if (failover == null) {
            return null;
        }
        String resolvedModel = providers().resolveChatModel(failover, null);
        log.warn("[AI Provider] failover to vendor={} model={}", failover.getProviderCode(), resolvedModel);
        return chatCache.computeIfAbsent(
                chatKey(failover, resolvedModel), key -> createChatClient(failover, resolvedModel));
    }

    public String failoverModelRef(String model) {
        AiProvider primary = providers().findChatProvider(model);
        Long excludeId = primary != null ? primary.getProviderId() : null;
        AiProvider failover = providers().findFailoverChatProvider(excludeId);
        if (failover == null) {
            return null;
        }
        String resolved = providers().resolveChatModel(failover, null);
        return AiModelRef.encode(failover.getProviderId(), resolved);
    }

    public static boolean isRetriableProviderError(Throwable error) {
        Throwable current = error;
        while (current != null) {
            String message = current.getMessage();
            if (StringUtils.hasText(message)) {
                String lower = message.toLowerCase();
                if (lower.contains("401")
                        || lower.contains("403")
                        || lower.contains("429")
                        || lower.contains("500")
                        || lower.contains("502")
                        || lower.contains("503")
                        || lower.contains("timeout")
                        || lower.contains("timed out")
                        || lower.contains("connection")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    public EmbeddingModel embeddingModel() {
        AiProvider provider = providers().findDefaultEmbeddingProvider();
        if (provider != null) {
            String model = StringUtils.hasText(provider.getDefaultEmbeddingModel())
                    ? provider.getDefaultEmbeddingModel().trim()
                    : "text-embedding-v3";
            log.debug(
                    "[AI Provider] use embedding vendor={} model={}",
                    provider.getProviderCode(),
                    model);
            return embeddingCache.computeIfAbsent(
                    embeddingKey(provider, model), key -> createEmbeddingModel(provider, model));
        }
        EmbeddingModel fallback = fallbackEmbeddingModel.getIfAvailable();
        if (fallback == null) {
            log.warn("[AI Provider] 无可用向量供应商，且 YAML Embedding 未配置");
        }
        return fallback;
    }

    public boolean hasEmbeddingCredential() {
        return embeddingModel() != null;
    }

    public AiProvider rerankProvider() {
        AiProviderService service = aiProviderService.getIfAvailable();
        return service != null ? service.findDefaultRerankProvider() : null;
    }

    private AiProviderService providers() {
        AiProviderService service = aiProviderService.getIfAvailable();
        if (service == null) {
            throw new IllegalStateException("AI 供应商服务尚未就绪");
        }
        return service;
    }

    public String pingChat(AiProvider provider) {
        String model = StringUtils.hasText(provider.getDefaultChatModel())
                ? provider.getDefaultChatModel().trim()
                : firstModel(provider);
        ChatClient client = createChatClient(provider, model);
        return client.prompt()
                .options(OpenAiChatOptions.builder().model(model).maxTokens(8).temperature(0.0).build())
                .user("ping")
                .call()
                .content();
    }

    public float[] embedWith(AiProvider provider, String text) {
        String model = StringUtils.hasText(provider.getDefaultEmbeddingModel())
                ? provider.getDefaultEmbeddingModel().trim()
                : "text-embedding-v3";
        EmbeddingModel embeddingModel = createEmbeddingModel(provider, model);
        return embeddingModel.embed(text);
    }

    private ChatClient createChatClient(AiProvider provider, String model) {
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(buildApi(provider))
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(StringUtils.hasText(model) ? model : "gpt-4o-mini")
                        .build())
                .build();
        log.info("[AI Provider] chat client created vendor={} model={}", provider.getProviderCode(), model);
        return ChatClient.create(chatModel);
    }

    private EmbeddingModel createEmbeddingModel(AiProvider provider, String model) {
        return new OpenAiEmbeddingModel(
                buildApi(provider),
                MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder().model(model).build());
    }

    private OpenAiApi buildApi(AiProvider provider) {
        OpenAiApi.Builder builder = OpenAiApi.builder()
                .baseUrl(normalizeBaseUrl(provider.getBaseUrl()))
                .apiKey(provider.getApiKey().trim());
        if (StringUtils.hasText(provider.getCompletionsPath())) {
            builder.completionsPath(provider.getCompletionsPath().trim());
        }
        if (StringUtils.hasText(provider.getEmbeddingsPath())) {
            builder.embeddingsPath(provider.getEmbeddingsPath().trim());
        }
        return builder.build();
    }

    private static String chatKey(AiProvider provider, String model) {
        return provider.getProviderId() + "|chat|" + provider.getBaseUrl() + "|" + model + "|"
                + Integer.toHexString(provider.getApiKey().hashCode());
    }

    private static String embeddingKey(AiProvider provider, String model) {
        return provider.getProviderId() + "|emb|" + provider.getBaseUrl() + "|" + model + "|"
                + Integer.toHexString(provider.getApiKey().hashCode());
    }

    private static String firstModel(AiProvider provider) {
        return StringUtils.hasText(provider.getDefaultChatModel()) ? provider.getDefaultChatModel() : "gpt-4o-mini";
    }

    static String normalizeBaseUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return url;
        }
        String value = url.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        if (value.endsWith("/v1")) {
            value = value.substring(0, value.length() - 3);
        }
        return value;
    }
}
