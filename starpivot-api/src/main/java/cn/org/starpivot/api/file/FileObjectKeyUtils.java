package cn.org.starpivot.api.file;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 文件中心 objectKey（OSS objectName）规范工具。
 * <p>
 * 格式：{@code {category.ossPrefix}{folderId}/{yyyy/MM/dd}/{uuid}{ext}}
 * <br>示例：{@code file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg}
 * </p>
 */
public final class FileObjectKeyUtils {

    /** 商城等业务上传的 objectKey 正则（不含 bucket 域名） */
    private static final Pattern OBJECT_KEY_PATTERN = Pattern.compile(
            "^file/[a-z]+/\\d+/\\d{4}/\\d{2}/\\d{2}/[0-9a-f\\-]+\\.[a-z0-9]+$");

    private FileObjectKeyUtils() {
    }

    /**
     * 判断是否为文件中心规范 objectKey。
     */
    public static boolean isFileCenterObjectKey(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return false;
        }
        String normalized = normalize(objectKey);
        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            return false;
        }
        return OBJECT_KEY_PATTERN.matcher(normalized).matches();
    }

    /**
     * 从 objectKey 解析业务分类 code（如 GOODS、OA）。
     */
    public static String resolveCategoryCode(String objectKey) {
        String normalized = normalize(objectKey);
        if (!normalized.startsWith("file/")) {
            return null;
        }
        String[] segments = normalized.split("/");
        if (segments.length < 3) {
            return null;
        }
        String segment = segments[1];
        for (FileCategory category : FileCategory.values()) {
            String prefix = category.getOssPrefix().replace("file/", "").replace("/", "");
            if (prefix.equalsIgnoreCase(segment)) {
                return category.getCode();
            }
        }
        return segment.toUpperCase();
    }

    /**
     * 去除首尾空白与多余斜杠。
     */
    public static String normalize(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return objectKey;
        }
        String normalized = objectKey.trim().replace('\\', '/').replaceAll("/{2,}", "/");
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }
}
