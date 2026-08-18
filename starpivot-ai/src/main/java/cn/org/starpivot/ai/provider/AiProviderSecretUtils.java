package cn.org.starpivot.ai.provider;

import org.springframework.util.StringUtils;

public final class AiProviderSecretUtils {

    private AiProviderSecretUtils() {}

    public static String mask(String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            return "";
        }
        String value = apiKey.trim();
        if (value.length() <= 8) {
            return "****";
        }
        return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
    }

    public static boolean isUnchanged(String incoming) {
        return !StringUtils.hasText(incoming) || incoming.contains("****");
    }
}
