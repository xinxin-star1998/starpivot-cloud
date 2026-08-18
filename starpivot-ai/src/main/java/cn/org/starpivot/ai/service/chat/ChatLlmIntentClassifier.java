package cn.org.starpivot.ai.service.chat;

import cn.org.starpivot.ai.config.AiProperties;
import cn.org.starpivot.ai.memory.ConversationTopicStore;
import cn.org.starpivot.ai.provider.AiModelClientFactory;
import cn.org.starpivot.ai.rag.HashUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatLlmIntentClassifier {

    private static final String CACHE_PREFIX = "ai:intent:v2:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(15);

    private final AiModelClientFactory aiModelClientFactory;
    private final StringRedisTemplate redisTemplate;
    private final AiProperties aiProperties;
    private final ChatQueryRouter chatQueryRouter;
    private final ConversationTopicStore conversationTopicStore;

    public ChatIntent refine(String message, ChatIntent ruleIntent) {
        return refine(message, ruleIntent, null, null);
    }

    public ChatIntent refine(String message, ChatIntent ruleIntent, ChatIntent previousIntent) {
        return refine(message, ruleIntent, previousIntent, null);
    }

    public ChatIntent refine(
            String message, ChatIntent ruleIntent, ChatIntent previousIntent, String conversationId) {
        if (!aiProperties.getQueryRouter().isLlmClassifyEnabled()) {
            return ruleIntent;
        }
        if (ruleIntent == null) {
            return ChatIntent.GENERAL;
        }
        if (!StringUtils.hasText(message) || message.trim().length() < 4) {
            return ruleIntent;
        }
        boolean shouldRefine =
                ruleIntent == ChatIntent.GENERAL || chatQueryRouter.isAmbiguous(message, ruleIntent);
        if (!shouldRefine) {
            return ruleIntent;
        }

        String previousHint = previousIntent != null ? previousIntent.name() : "";
        String topicHint = conversationTopicStore.loadHint(conversationId);
        String cacheKey = CACHE_PREFIX
                + HashUtils.md5(message.trim() + "|" + ruleIntent.name() + "|" + previousHint + "|" + topicHint);
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cached)) {
            return parseIntent(cached, ruleIntent);
        }

        String previousLine = StringUtils.hasText(previousHint)
                ? "上一轮意图：" + previousHint + "（若本轮是简短追问且未换任务，可沿用上一轮）\n"
                : "";
        String topicLine = StringUtils.hasText(topicHint) ? "会话关注点：" + topicHint + "\n" : "";
        String prompt = """
                将用户消息分类为以下之一，只输出意图英文名：
                CHITCHAT（寒暄致谢）、KNOWLEDGE（产品/配置/操作/文档/菜单权限）、DEVELOPER（编程调试代码报错）、ANALYST（数据指标报表）、REASONING（复杂架构/权衡推理）、GENERAL（其他写作翻译闲聊任务）。
                规则初判：%s
                若同时像编程又像平台操作，按用户主诉求选择；问“怎么在平台里配置/操作”优先 KNOWLEDGE，问“代码怎么写/报错怎么修”优先 DEVELOPER。
                %s%s用户消息：%s
                """.formatted(ruleIntent.name(), previousLine, topicLine, truncate(message.trim(), 400));
        try {
            String raw = aiModelClientFactory.chatClient().prompt().user(prompt).call().content();
            ChatIntent intent = parseIntent(raw, ruleIntent);
            redisTemplate.opsForValue().set(cacheKey, intent.name(), CACHE_TTL);
            if (intent != ruleIntent) {
                log.info("[QueryRouter] llm classify {} -> {}", ruleIntent, intent);
            }
            return intent;
        } catch (Exception ex) {
            log.debug("[QueryRouter] llm classify failed: {}", ex.getMessage());
            return ruleIntent;
        }
    }

    static ChatIntent parseIntent(String raw) {
        return parseIntent(raw, ChatIntent.GENERAL);
    }

    static ChatIntent parseIntent(String raw, ChatIntent fallback) {
        if (!StringUtils.hasText(raw)) {
            return fallback != null ? fallback : ChatIntent.GENERAL;
        }
        String text = raw.trim().toUpperCase();
        for (ChatIntent intent : ChatIntent.values()) {
            if (text.contains(intent.name())) {
                return intent;
            }
        }
        return fallback != null ? fallback : ChatIntent.GENERAL;
    }

    private static String truncate(String text, int maxLen) {
        return text.length() <= maxLen ? text : text.substring(0, maxLen);
    }
}
