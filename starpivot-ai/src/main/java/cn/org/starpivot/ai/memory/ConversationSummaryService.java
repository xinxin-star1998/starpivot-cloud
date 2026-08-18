package cn.org.starpivot.ai.memory;

import cn.org.starpivot.ai.config.AiProperties;
import cn.org.starpivot.ai.metrics.AiMetrics;
import cn.org.starpivot.ai.provider.AiModelClientFactory;
import cn.org.starpivot.ai.rag.HashUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConversationSummaryService {

    private static final String SUMMARY_PREFIX = "ai:summary:v1:";
    private static final String FINGERPRINT_SUFFIX = ":fp";
    private static final Duration SUMMARY_TTL = Duration.ofDays(7);

    private final AiModelClientFactory aiModelClientFactory;
    private final MessageWindowChatMemory chatMemory;
    private final StringRedisTemplate redisTemplate;
    private final AiProperties aiProperties;
    private final AiMetrics aiMetrics;
    private final Executor memorySummaryExecutor;

    public ConversationSummaryService(
            AiModelClientFactory aiModelClientFactory,
            MessageWindowChatMemory chatMemory,
            StringRedisTemplate redisTemplate,
            AiProperties aiProperties,
            AiMetrics aiMetrics,
            @Qualifier("memorySummaryExecutor") Executor memorySummaryExecutor) {
        this.aiModelClientFactory = aiModelClientFactory;
        this.chatMemory = chatMemory;
        this.redisTemplate = redisTemplate;
        this.aiProperties = aiProperties;
        this.aiMetrics = aiMetrics;
        this.memorySummaryExecutor = memorySummaryExecutor;
    }

    public boolean isEnabled() {
        return aiProperties.getMemory().isSummaryEnabled();
    }

    public String getSummary(String conversationId) {
        if (!isEnabled() || !StringUtils.hasText(conversationId)) {
            return "";
        }
        String summary = redisTemplate.opsForValue().get(SUMMARY_PREFIX + conversationId);
        return summary != null ? summary : "";
    }

    public void refreshAsync(String conversationId) {
        if (!isEnabled() || !StringUtils.hasText(conversationId)) {
            return;
        }
        memorySummaryExecutor.execute(() -> refresh(conversationId));
    }

    public void clear(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return;
        }
        redisTemplate.delete(List.of(
                SUMMARY_PREFIX + conversationId,
                SUMMARY_PREFIX + conversationId + FINGERPRINT_SUFFIX));
    }

    private void refresh(String conversationId) {
        try {
            List<Message> messages = chatMemory.get(conversationId);
            if (messages == null) {
                return;
            }
            int minMessages = Math.max(aiProperties.getMemory().getSummaryMinMessages(), 6);
            int keepRecent = Math.max(aiProperties.getMemory().getSummaryKeepRecent(), 2);
            if (messages.size() < minMessages || messages.size() <= keepRecent) {
                return;
            }

            List<Message> older = messages.subList(0, messages.size() - keepRecent);
            String fingerprint = HashUtils.md5(older.stream()
                    .map(message -> message.getMessageType().name() + ":" + nullToEmpty(message.getText()))
                    .collect(Collectors.joining("\n")));
            String fpKey = SUMMARY_PREFIX + conversationId + FINGERPRINT_SUFFIX;
            if (fingerprint.equals(redisTemplate.opsForValue().get(fpKey))) {
                return;
            }

            long start = System.currentTimeMillis();
            String existing = getSummary(conversationId);
            String transcript = older.stream()
                    .map(message -> message.getMessageType().name() + ": "
                            + truncate(nullToEmpty(message.getText()), 280))
                    .collect(Collectors.joining("\n"));
            String prompt = """
                    请把以下对话压缩为不超过 180 字的中文摘要，保留用户目标、已确认事实、未解决问题与关键约束。
                    不要编造新信息。只输出摘要正文。

                    已有摘要：
                    %s

                    需要压缩的对话：
                    %s
                    """.formatted(StringUtils.hasText(existing) ? existing : "（无）", transcript);

            String summary = aiModelClientFactory.chatClient().prompt().user(prompt).call().content();
            if (!StringUtils.hasText(summary)) {
                return;
            }
            String normalized = truncate(summary.trim(), 600);
            redisTemplate.opsForValue().set(SUMMARY_PREFIX + conversationId, normalized, SUMMARY_TTL);
            redisTemplate.opsForValue().set(fpKey, fingerprint, SUMMARY_TTL);
            aiMetrics.recordMemorySummary(System.currentTimeMillis() - start);
            log.info("[Memory] conversation summary refreshed conversationIdHash={} chars={}",
                    HashUtils.md5(conversationId).substring(0, 8), normalized.length());
        } catch (Exception ex) {
            log.debug("[Memory] summary refresh skipped: {}", ex.getMessage());
        }
    }

    private static String nullToEmpty(String text) {
        return text != null ? text : "";
    }

    private static String truncate(String text, int maxLen) {
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen);
    }
}
