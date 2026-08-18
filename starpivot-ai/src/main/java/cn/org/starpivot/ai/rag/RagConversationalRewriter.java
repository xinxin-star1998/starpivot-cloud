package cn.org.starpivot.ai.rag;

import cn.org.starpivot.ai.config.AiProperties;
import cn.org.starpivot.ai.memory.ConversationTopicStore;
import cn.org.starpivot.ai.metrics.AiMetrics;
import cn.org.starpivot.ai.provider.AiModelClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagConversationalRewriter {

    private static final String CACHE_PREFIX = "ai:qrewrite:v1:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);
    private static final Pattern FOLLOW_UP = Pattern.compile(
            "这|那|它|其|上述|刚才|刚刚|还是|还有|继续|同样|上面|之前|这个|那个|这种|那种|然后呢|呢$|吗$");

    private final AiModelClientFactory aiModelClientFactory;
    private final MessageWindowChatMemory chatMemory;
    private final StringRedisTemplate redisTemplate;
    private final AiProperties aiProperties;
    private final AiMetrics aiMetrics;
    private final ConversationTopicStore conversationTopicStore;

    public boolean isEnabled() {
        return aiProperties.getRag().isQueryRewriteEnabled();
    }

    /**
     * 将指代性追问改写为可独立检索的完整问句。
     *
     * @param forRetrieval true 表示本轮需要检索（预 RAG 或 Agent 知识库工具）
     */
    public String rewrite(String conversationId, String question, boolean forRetrieval) {
        if (!forRetrieval || !isEnabled() || !StringUtils.hasText(question)) {
            return question;
        }
        List<Message> history = loadHistory(conversationId);
        if (!shouldRewrite(question.trim(), history.size())) {
            return question;
        }

        long start = System.currentTimeMillis();
        String rewritten = doRewrite(conversationId, question.trim(), history);
        aiMetrics.recordRagStage("query_rewrite", System.currentTimeMillis() - start);
        if (!StringUtils.hasText(rewritten)) {
            return question;
        }
        log.info("[RAG] query rewrite originalLen={} rewrittenLen={}", question.length(), rewritten.length());
        return rewritten;
    }

    static boolean shouldRewrite(String message, int historySize) {
        if (historySize <= 0 || !StringUtils.hasText(message)) {
            return false;
        }
        String text = message.trim();
        // 仅在有指代/追问特征时改写，避免短句每轮都打一轮 LLM
        return FOLLOW_UP.matcher(text).find();
    }

    private String doRewrite(String conversationId, String question, List<Message> history) {
        String digest = history.stream()
                .map(message -> message.getMessageType().name() + ":" + safeText(message.getText()))
                .collect(Collectors.joining("\n"));
        String topicHint = conversationTopicStore.loadHint(conversationId);
        String cacheKey = CACHE_PREFIX + HashUtils.md5(digest + "\n" + question + "\n" + topicHint);
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cached)) {
            return cached;
        }

        String historyBlock = history.stream()
                .skip(Math.max(0, history.size() - 6))
                .map(message -> message.getMessageType().name() + ": " + truncate(safeText(message.getText()), 240))
                .collect(Collectors.joining("\n"));
        String topicLine = StringUtils.hasText(topicHint) ? "会话关注点：" + topicHint + "\n" : "";

        String prompt = """
                将用户的最新问题改写成独立、完整、可检索的查询，补全对话中被省略的对象与指代。
                若问题本身已经完整，原样返回。
                只输出改写后的查询，不要解释，不要加引号。
                %s
                对话历史：
                %s

                最新问题：%s
                """.formatted(topicLine, historyBlock, question);

        try {
            String rewritten = aiModelClientFactory.chatClient().prompt().user(prompt).call().content();
            if (!StringUtils.hasText(rewritten)) {
                return question;
            }
            String normalized = rewritten.trim().replaceAll("^[\"“”']+|[\"“”']+$", "");
            if (normalized.length() > 200) {
                normalized = normalized.substring(0, 200);
            }
            redisTemplate.opsForValue().set(cacheKey, normalized, CACHE_TTL);
            return normalized;
        } catch (Exception ex) {
            log.warn("[RAG] query rewrite failed, fallback to original: {}", ex.getMessage());
            return question;
        }
    }

    private List<Message> loadHistory(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return List.of();
        }
        try {
            List<Message> history = chatMemory.get(conversationId);
            return history != null ? history : List.of();
        } catch (RuntimeException ex) {
            log.debug("[RAG] load history for rewrite failed: {}", ex.getMessage());
            return List.of();
        }
    }

    private static String safeText(String text) {
        return text != null ? text : "";
    }

    private static String truncate(String text, int maxLen) {
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen) + "...";
    }
}
