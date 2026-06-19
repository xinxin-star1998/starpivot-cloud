package cn.org.starpivot.common.storage;

import org.springframework.util.StringUtils;

import java.net.URI;

/**
 * OSS 对象路径工具：统一存库格式为 objectName（如 goods/2026/06/01/xxx.jpg）
 */
public final class StorageObjectPathUtils {

    private static final String[] STORAGE_PREFIXES = {"goods/", "editor/", "avatar/"};

    private StorageObjectPathUtils() {
    }

    /**
     * 将完整 URL 或 objectName 规范化为 objectName；无法识别时返回 trim 后的原值
     */
    public static String normalizeToObjectName(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String trimmed = value.trim();
        if (isStorageObjectName(trimmed)) {
            return trimmed;
        }
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            String extracted = extractObjectNameFromUrl(trimmed);
            if (extracted != null) {
                return extracted;
            }
        }
        return trimmed;
    }

    public static boolean isStorageObjectName(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return false;
        }
        for (String prefix : STORAGE_PREFIXES) {
            if (value.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static String extractObjectNameFromUrl(String url) {
        try {
            String withoutQuery = url.split("\\?", 2)[0];
            URI uri = URI.create(withoutQuery);
            String path = uri.getPath();
            if (!StringUtils.hasText(path)) {
                return null;
            }
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            for (String prefix : STORAGE_PREFIXES) {
                int idx = path.indexOf(prefix);
                if (idx >= 0) {
                    return path.substring(idx);
                }
            }
        } catch (Exception ignored) {
            // fall through
        }
        return null;
    }
}

