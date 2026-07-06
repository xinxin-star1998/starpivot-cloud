package cn.org.starpivot.ai.rag;

import cn.org.starpivot.ai.config.AiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagQueryRewriterService {

    private static final String HYDE_CACHE_PREFIX = "ai:hyde:v1:";
    private static final Duration HYDE_CACHE_TTL = Duration.ofMinutes(10);

    private final ChatClient chatClient;
    private final StringRedisTemplate redisTemplate;
    private final AiProperties aiProperties;

    public boolean isEnabled() {
        return aiProperties.getRag().isHydeEnabled();
    }

    public String generateHypotheticalAnswer(String question) {
        if (!StringUtils.hasText(question)) {
            return question;
        }
        String cacheKey = HYDE_CACHE_PREFIX + question.hashCode();
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cached)) {
            return cached;
        }

        String prompt = """
                请根据以下问题，生成一个简洁的假设性回答（2-4句话）。
                这个回答不需要准确，只需要在语义上覆盖可能的答案内容。
                直接给出答案内容，不要任何前缀说明。

                问题：%s
                """.formatted(question.trim());

        try {
            String hypothetical = chatClient.prompt().user(prompt).call().content();
            if (!StringUtils.hasText(hypothetical)) {
                return question;
            }
            String normalized = hypothetical.trim();
            redisTemplate.opsForValue().set(cacheKey, normalized, HYDE_CACHE_TTL);
            log.debug("[HyDE] generated for question length={}", question.length());
            return normalized;
        } catch (Exception ex) {
            log.warn("[HyDE] generation failed, fallback to original question: {}", ex.getMessage());
            return question;
        }
    }
}
