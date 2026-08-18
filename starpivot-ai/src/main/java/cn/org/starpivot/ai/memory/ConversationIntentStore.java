package cn.org.starpivot.ai.memory;

import cn.org.starpivot.ai.service.chat.ChatIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * 记住上一轮任务意图，供短追问沿用。寒暄不覆盖，写作/翻译等 GENERAL 会清掉粘性任务。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConversationIntentStore {

    private static final String KEY_PREFIX = "ai:last-intent:v1:";
    private static final Duration TTL = Duration.ofHours(2);

    private final StringRedisTemplate redisTemplate;

    public ChatIntent load(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return null;
        }
        try {
            String raw = redisTemplate.opsForValue().get(KEY_PREFIX + conversationId);
            if (!StringUtils.hasText(raw)) {
                return null;
            }
            return ChatIntent.valueOf(raw.trim());
        } catch (Exception ex) {
            log.debug("[QueryRouter] load last intent failed: {}", ex.getMessage());
            return null;
        }
    }

    public void save(String conversationId, ChatIntent intent) {
        if (!StringUtils.hasText(conversationId) || intent == null || intent == ChatIntent.CHITCHAT) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + conversationId, intent.name(), TTL);
        } catch (Exception ex) {
            log.debug("[QueryRouter] save last intent failed: {}", ex.getMessage());
        }
    }
}
