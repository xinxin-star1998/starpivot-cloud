package cn.org.starpivot.common.storage;

import org.springframework.util.StringUtils;

/**
 * OSS 对象路径安全校验
 */
public final class StoragePathValidator {

    /** 允许通过 API 获取预签名 URL 的路径前缀 */
    public static final String[] ALLOWED_PRESIGNED_PREFIXES = {"editor/", "goods/", "avatar/", "brand/", "file/"};

    private StoragePathValidator() {
    }

    /**
     * 校验对象路径是否允许获取预签名 URL
     */
    public static boolean isAllowedPresignedPath(String objectName) {
        if (!StringUtils.hasText(objectName) || objectName.contains("..") || objectName.startsWith("/")) {
            return false;
        }
        for (String prefix : ALLOWED_PRESIGNED_PREFIXES) {
            if (objectName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}

