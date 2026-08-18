package cn.org.starpivot.ai.memory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 会话级主题/实体记忆，帮助追问改写与意图路由保持同一话题。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConversationTopicStore {

    private static final String KEY_PREFIX = "ai:topics:v1:";
    private static final Duration TTL = Duration.ofHours(2);
    private static final int MAX_TOPICS = 3;

    private final StringRedisTemplate redisTemplate;
    private final ConversationTopicExtractor conversationTopicExtractor;

    public String loadHint(String conversationId) {
        List<String> topics = load(conversationId);
        if (topics.isEmpty()) {
            return "";
        }
        return String.join("、", topics);
    }

    public List<String> load(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return List.of();
        }
        try {
            String raw = redisTemplate.opsForValue().get(KEY_PREFIX + conversationId.trim());
            if (!StringUtils.hasText(raw)) {
                return List.of();
            }
            return Arrays.stream(raw.split("\\|"))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .limit(MAX_TOPICS)
                    .toList();
        } catch (Exception ex) {
            log.debug("[TopicMemory] load failed: {}", ex.getMessage());
            return List.of();
        }
    }

    public void rememberFromMessage(String conversationId, String message) {
        List<String> extracted = conversationTopicExtractor.extract(message);
        if (extracted.isEmpty()) {
            return;
        }
        merge(conversationId, extracted);
    }

    public void merge(String conversationId, List<String> incoming) {
        if (!StringUtils.hasText(conversationId) || incoming == null || incoming.isEmpty()) {
            return;
        }
        try {
            Set<String> merged = new LinkedHashSet<>();
            for (String item : incoming) {
                if (StringUtils.hasText(item)) {
                    merged.add(item.trim());
                }
            }
            for (String existing : load(conversationId)) {
                if (merged.size() >= MAX_TOPICS) {
                    break;
                }
                merged.add(existing);
            }
            List<String> limited = new ArrayList<>(merged).stream().limit(MAX_TOPICS).toList();
            redisTemplate.opsForValue().set(
                    KEY_PREFIX + conversationId.trim(),
                    limited.stream().collect(Collectors.joining("|")),
                    TTL);
        } catch (Exception ex) {
            log.debug("[TopicMemory] save failed: {}", ex.getMessage());
        }
    }

    public void clear(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return;
        }
        try {
            redisTemplate.delete(KEY_PREFIX + conversationId.trim());
        } catch (Exception ex) {
            log.debug("[TopicMemory] clear failed: {}", ex.getMessage());
        }
    }
}
