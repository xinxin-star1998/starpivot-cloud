package cn.org.starpivot.ai.provider;

import org.springframework.util.StringUtils;

/**
 * 对话模型引用：{@code p{providerId}:{modelId}}，避免跨供应商同名冲突。
 * 无 provider 前缀时按裸 modelId 兼容 YAML / 旧会话。
 */
public final class AiModelRef {

    private static final String PREFIX = "p";
    private static final char SEP = ':';

    private AiModelRef() {}

    public static String encode(Long providerId, String modelId) {
        if (providerId == null || !StringUtils.hasText(modelId)) {
            return StringUtils.hasText(modelId) ? modelId.trim() : null;
        }
        return PREFIX + providerId + SEP + modelId.trim();
    }

    public static Long providerId(String ref) {
        Parsed parsed = parse(ref);
        return parsed != null ? parsed.providerId() : null;
    }

    public static String modelId(String ref) {
        Parsed parsed = parse(ref);
        if (parsed != null) {
            return parsed.modelId();
        }
        return StringUtils.hasText(ref) ? ref.trim() : null;
    }

    /** 供意图选模时做名称匹配（去掉供应商前缀） */
    public static String matchKey(String ref) {
        return modelId(ref);
    }

    public static Parsed parse(String ref) {
        if (!StringUtils.hasText(ref)) {
            return null;
        }
        String value = ref.trim();
        if (!value.startsWith(PREFIX)) {
            return new Parsed(null, value);
        }
        int sep = value.indexOf(SEP);
        if (sep <= 1) {
            return new Parsed(null, value);
        }
        String idPart = value.substring(1, sep);
        String model = value.substring(sep + 1).trim();
        if (!StringUtils.hasText(model) || !idPart.chars().allMatch(Character::isDigit)) {
            return new Parsed(null, value);
        }
        try {
            return new Parsed(Long.parseLong(idPart), model);
        } catch (NumberFormatException ex) {
            return new Parsed(null, value);
        }
    }

    public record Parsed(Long providerId, String modelId) {}
}
