package cn.org.starpivot.common.storage.impl;

import cn.org.starpivot.common.storage.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地磁盘文件存储（OSS 未启用时的开发/测试兜底）。
 * <p>
 * 当 {@code oss.enabled=false} 或未配置时激活，实现 {@link FileStorageService}。
 * 文件保存在 {@code file.storage.local-path} 目录，访问 URL 前缀为 {@code /local-storage/}。
 * </p>
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "oss", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private final Path baseDir;

    /**
     * 初始化本地存储根目录，不存在则自动创建。
     *
     * @param localPath 本地存储路径，默认 {@code ${user.home}/starpivot/uploads}
     */
    public LocalFileStorageService(
            @Value("${file.storage.local-path:${user.home}/starpivot/uploads}") String localPath) {
        this.baseDir = Paths.get(localPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建本地存储目录: " + baseDir, e);
        }
        log.info("Local file storage enabled, baseDir={}", baseDir);
    }

    /** {@inheritDoc} */
    @Override
    public String uploadAvatar(MultipartFile file, String userId) throws Exception {
        String suffix = getFileSuffix(file.getOriginalFilename(), IMAGE_MIME_TYPES);
        String objectName = "avatar/" + userId + suffix;
        uploadFileInternal(file, objectName);
        return objectName;
    }

    /** {@inheritDoc} */
    @Override
    public String uploadAvatarWithUrl(MultipartFile file, String userId) throws Exception {
        return getPermanentUrl(uploadAvatar(file, userId));
    }

    /** {@inheritDoc} 本地存储无预签名概念，等同 {@link #uploadAvatarWithUrl} */
    @Override
    public String uploadAvatarWithPresignedUrl(MultipartFile file, String userId) throws Exception {
        return uploadAvatarWithUrl(file, userId);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteAvatar(String userId) throws Exception {
        Path avatarDir = baseDir.resolve("avatar");
        if (!Files.isDirectory(avatarDir)) {
            return;
        }
        try (var stream = Files.list(avatarDir)) {
            stream.filter(path -> path.getFileName().toString().startsWith(userId + "."))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.warn("Failed to delete avatar file: {}", path, e);
                        }
                    });
        }
    }

    /** {@inheritDoc} 本地存储返回永久访问路径 */
    @Override
    public String getPresignedUrl(String objectName) throws Exception {
        return getPermanentUrl(objectName);
    }

    /** {@inheritDoc} */
    @Override
    public String getPermanentUrl(String objectName) {
        return "/local-storage/" + normalizeObjectName(objectName);
    }

    /** {@inheritDoc} */
    @Override
    public String uploadEditorImageWithUrl(MultipartFile file) throws Exception {
        String objectName = generateObjectName("editor", null, file.getOriginalFilename(), IMAGE_MIME_TYPES);
        uploadFileInternal(file, objectName);
        return getPermanentUrl(objectName);
    }

    /** {@inheritDoc} */
    @Override
    public void uploadFileInternal(MultipartFile file, String objectName) throws Exception {
        Path target = resolveObjectPath(objectName);
        Files.createDirectories(target.getParent());
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void deleteObject(String objectName) throws Exception {
        Files.deleteIfExists(resolveObjectPath(objectName));
    }

    private Path resolveObjectPath(String objectName) {
        Path target = baseDir.resolve(normalizeObjectName(objectName)).normalize();
        if (!target.startsWith(baseDir)) {
            throw new IllegalArgumentException("非法文件路径");
        }
        return target;
    }

    private static String normalizeObjectName(String objectName) {
        return StringUtils.cleanPath(objectName.replace('\\', '/'));
    }

    private static final String[] IMAGE_MIME_TYPES =
            {"image/jpeg", "image/png", "image/gif", "image/webp"};
}
