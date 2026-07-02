package cn.org.starpivot.mall.pms.support;

import cn.org.starpivot.common.storage.FileStorageService;
import cn.org.starpivot.common.storage.StoragePathValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商城 C 端图片展示 URL 解析（预签名 / 永久链 / 本地存储绝对路径）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MallImageDisplaySupport {

    private final FileStorageService fileStorageService;

    @Value("${starpivot.mall.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    public Map<String, String> resolveDisplayUrls(List<String> objectNames) {
        if (objectNames == null || objectNames.isEmpty()) {
            return Map.of();
        }
        Map<String, String> urls = new LinkedHashMap<>();
        for (String raw : objectNames) {
            if (!StringUtils.hasText(raw)) {
                continue;
            }
            String trimmed = raw.trim();
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                urls.put(raw, trimmed);
                continue;
            }
            trimmed = normalizeObjectName(trimmed);
            if (!StoragePathValidator.isAllowedPresignedPath(trimmed)) {
                continue;
            }
            String displayUrl = resolveOne(trimmed);
            if (StringUtils.hasText(displayUrl)) {
                urls.put(raw, displayUrl);
            }
        }
        return urls;
    }

    private String resolveOne(String objectName) {
        try {
            return toAbsoluteUrl(fileStorageService.getPresignedUrl(objectName));
        } catch (Exception ex) {
            log.debug("presigned url failed for {}: {}", objectName, ex.getMessage());
        }
        try {
            return toAbsoluteUrl(fileStorageService.getPermanentUrl(objectName));
        } catch (Exception ex) {
            log.warn("display url failed for {}", objectName, ex);
            return null;
        }
    }

    private String toAbsoluteUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return url;
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        if (url.startsWith("/")) {
            String base = publicBaseUrl.endsWith("/")
                    ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                    : publicBaseUrl;
            return base + url;
        }
        return url;
    }

    private static String normalizeObjectName(String objectName) {
        String normalized = objectName.replace('\\', '/').replaceAll("/{2,}", "/");
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }
}
