package cn.org.starpivot.ai.service.chat;

import cn.org.starpivot.ai.config.AiProperties;
import cn.org.starpivot.ai.config.AiRuntimeSnapshot;
import cn.org.starpivot.ai.provider.AiModelRef;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 按当前供应商可用模型选择路由模型，YAML 中的 chat/reasoner 仅作偏好提示。
 */
public final class SmartModelSelector {

    private static final Pattern REASONING = Pattern.compile(
            "reasoner|thinking|v4-pro|kimi-k3|k2\\.7-code|glm-5|o1-|o3-|-r1",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern DEVELOPER = Pattern.compile("coder|-code|code-", Pattern.CASE_INSENSITIVE);

    private SmartModelSelector() {}

    public static String pick(ChatIntent intent, AiProperties.QueryRouterProperties router, AiRuntimeSnapshot runtime) {
        String chatFallback = router.resolvedChatModel(defaultModel(runtime, null));
        String chatModel = firstAllowed(runtime, chatFallback, null);
        return switch (intent) {
            case REASONING -> pickReasoner(router, runtime, chatModel);
            case DEVELOPER -> pickDeveloper(runtime, chatModel);
            default -> chatModel;
        };
    }

    private static String pickReasoner(
            AiProperties.QueryRouterProperties router, AiRuntimeSnapshot runtime, String chatModel) {
        String configured = router.resolvedReasonerModel(null);
        String matched = firstAllowed(runtime, configured, REASONING);
        return StringUtils.hasText(matched) ? matched : chatModel;
    }

    private static String pickDeveloper(AiRuntimeSnapshot runtime, String chatModel) {
        String matched = firstAllowed(runtime, null, DEVELOPER);
        return StringUtils.hasText(matched) ? matched : chatModel;
    }

    private static String firstAllowed(AiRuntimeSnapshot runtime, String preferred, Pattern pattern) {
        List<String> ids = modelIds(runtime);
        if (StringUtils.hasText(preferred)) {
            String hit = findByBareOrExact(ids, preferred.trim());
            if (hit != null) {
                return hit;
            }
        }
        if (pattern != null) {
            for (String id : ids) {
                if (pattern.matcher(AiModelRef.matchKey(id)).find()) {
                    return id;
                }
            }
        }
        return defaultModel(runtime, StringUtils.hasText(preferred) ? preferred.trim() : null);
    }

    private static String findByBareOrExact(List<String> ids, String preferred) {
        if (ids.contains(preferred)) {
            return preferred;
        }
        String bare = AiModelRef.modelId(preferred);
        for (String id : ids) {
            if (bare != null && bare.equals(AiModelRef.modelId(id))) {
                return id;
            }
        }
        return null;
    }

    private static List<String> modelIds(AiRuntimeSnapshot runtime) {
        if (runtime == null || runtime.getModels() == null || runtime.getModels().isEmpty()) {
            return List.of();
        }
        return runtime.getModels().stream()
                .filter(item -> item != null && StringUtils.hasText(item.getId()))
                .map(item -> item.getId().trim())
                .toList();
    }

    private static String defaultModel(AiRuntimeSnapshot runtime, String fallback) {
        if (runtime != null && StringUtils.hasText(runtime.getDefaultModel())) {
            return runtime.getDefaultModel().trim();
        }
        return fallback;
    }
}
