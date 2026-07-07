package cn.org.starpivot.common.util;

import cn.org.starpivot.common.config.OssProperties;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 阿里云 OSS 工具类
 * 用于文件上传、删除、获取预签名URL等操作
 */
@Component
@ConditionalOnProperty(prefix = "oss", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class OssUtil {

    /** 头像大小限制 2MB */
    private static final long AVATAR_MAX_SIZE = 2 * 1024 * 1024;
    /** 富文本编辑器图片大小限制 3MB（与前端 WangEditor 默认一致） */
    private static final long EDITOR_IMAGE_MAX_SIZE = 3 * 1024 * 1024;
    /** 允许的头像 Content-Type */
    private static final String[] ALLOWED_AVATAR_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};
    /** userId 仅允许数字，防止路径穿越 */
    private static final Pattern USER_ID_PATTERN = Pattern.compile("^[0-9]+$");

    private final OSS ossClient;
    private final OssProperties ossProperties;

    private String endpoint() {
        return ossProperties.getEndpoint();
    }

    private String accessKeyId() {
        return ossProperties.getAccessKeyId();
    }

    private String accessKeySecret() {
        return ossProperties.getAccessKeySecret();
    }

    private String bucketName() {
        return ossProperties.getBucketName();
    }

    private String urlPrefix() {
        return ossProperties.getUrlPrefix();
    }

    /**
     * 校验 userId 格式，仅允许纯数字，防止路径穿越
     */
    private void validateUserId(String userId) {
        if (!StringUtils.hasText(userId) || !USER_ID_PATTERN.matcher(userId).matches()) {
            throw new IllegalArgumentException("用户ID格式无效,仅允许数字");
        }
    }

    /**
     * 校验头像文件：大小与类型
     */
    private void checkEditorImageValid(MultipartFile file) {
        if (file.getSize() > EDITOR_IMAGE_MAX_SIZE) {
            throw new IllegalArgumentException("图片大小不能超过3MB");
        }
        String contentType = file.getContentType();
        if (contentType == null) {
            contentType = "";
        }
        boolean allowed = false;
        for (String t : ALLOWED_AVATAR_TYPES) {
            if (t.equals(contentType)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new IllegalArgumentException("仅支持 JPG、PNG、GIF、WEBP 格式图片");
        }
    }

    private void checkAvatarFileValid(MultipartFile file) {
        if (file.getSize() > AVATAR_MAX_SIZE) {
            throw new IllegalArgumentException("头像文件大小不能超过2MB");
        }
        String contentType = file.getContentType();
        if (contentType == null) {
            contentType = "";
        }
        boolean allowed = false;
        for (String t : ALLOWED_AVATAR_TYPES) {
            if (t.equals(contentType)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new IllegalArgumentException("头像仅支持JPG、PNG、GIF、WEBP格式");
        }
    }

    /**
     * 安全获取文件后缀，仅允许图片扩展名，默认 .png
     */
    private String getSafeImageSuffix(String originalFilename) {
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            return ".png";
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (".jpeg".equals(ext) || ".jpg".equals(ext)) {
            return ".jpg";
        }
        if (".png".equals(ext) || ".gif".equals(ext) || ".webp".equals(ext)) {
            return ext;
        }
        return ".png";
    }

    /**
     * 解析头像对象路径：库中记录的后缀与 OSS 实际文件不一致时（如 .jpeg vs .jpg），按 userId 查找真实对象。
     */
    private String resolveAvatarObjectName(String objectName) {
        if (!StringUtils.hasText(objectName) || !objectName.startsWith("avatar/")) {
            return objectName;
        }
        if (ossClient.doesObjectExist(bucketName(), objectName)) {
            return objectName;
        }
        String fileName = objectName.substring("avatar/".length());
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0) {
            return objectName;
        }
        String userId = fileName.substring(0, dotIndex);
        if (!USER_ID_PATTERN.matcher(userId).matches()) {
            return objectName;
        }
        String prefix = "avatar/" + userId;
        ListObjectsV2Request listRequest = new ListObjectsV2Request()
                .withBucketName(bucketName())
                .withPrefix(prefix);
        ListObjectsV2Result listResult = ossClient.listObjectsV2(listRequest);
        for (OSSObjectSummary summary : listResult.getObjectSummaries()) {
            String key = summary.getKey();
            if (key.startsWith(prefix + ".")) {
                return key;
            }
        }
        return objectName;
    }

    /**
     * 上传头像文件
     * @param file 头像文件
     * @param userId 用户ID（仅数字）
     * @return 头像文件在存储桶中的路径
     */
    public String uploadAvatar(MultipartFile file, String userId) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传的头像文件不能为空");
        }
        validateUserId(userId);
        checkAvatarFileValid(file);

        String suffix = getSafeImageSuffix(file.getOriginalFilename());
        String objectName = "avatar/" + userId + suffix;

        // 先上传新文件（避免先删后传导致上传失败时丢失旧头像）
        ObjectMetadata metadata = new ObjectMetadata();
        String contentType = file.getContentType();
        metadata.setContentType(StringUtils.hasText(contentType) ? contentType : "image/png");
        metadata.setContentLength(file.getSize());
        PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucketName(),
                objectName,
                file.getInputStream()
        );
        putObjectRequest.setMetadata(metadata);
        ossClient.putObject(putObjectRequest);

        // 再删除该用户下其它旧头像（不同后缀的旧文件）
        String prefix = "avatar/" + userId;
        ListObjectsV2Request listRequest = new ListObjectsV2Request()
                .withBucketName(bucketName())
                .withPrefix(prefix);
        ListObjectsV2Result listResult = ossClient.listObjectsV2(listRequest);
        for (OSSObjectSummary summary : listResult.getObjectSummaries()) {
            if (!objectName.equals(summary.getKey())) {
                ossClient.deleteObject(bucketName(), summary.getKey());
            }
        }
        return objectName;
    }

    /**
     * 上传富文本编辑器图片，返回 OSS 对象路径（如 editor/yyyy/MM/dd/{uuid}.jpg）。
     * <p>HTML 中应存对象路径而非预签名 URL，展示时再通过预签名接口换取临时链接。</p>
     */
    public String uploadEditorImage(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传的图片不能为空");
        }
        checkEditorImageValid(file);
        String suffix = getSafeImageSuffix(file.getOriginalFilename());
        String day = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String objectName = "editor/" + day + "/" + UUID.randomUUID() + suffix;

        ObjectMetadata metadata = new ObjectMetadata();
        String contentType = file.getContentType();
        metadata.setContentType(StringUtils.hasText(contentType) ? contentType : "image/png");
        metadata.setContentLength(file.getSize());
        PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucketName(),
                objectName,
                file.getInputStream()
        );
        putObjectRequest.setMetadata(metadata);
        ossClient.putObject(putObjectRequest);
        return objectName;
    }

    /**
     * 上传富文本编辑器图片并返回对象路径（兼容旧接口名，不再返回预签名 URL）。
     */
    public String uploadEditorImageWithUrl(MultipartFile file) throws Exception {
        return uploadEditorImage(file);
    }

    /**
     * 上传头像文件并返回完整访问URL
     * @param file 头像文件
     * @param userId 用户ID
     * @return 完整的头像访问URL
     */
    public String uploadAvatarWithUrl(MultipartFile file, String userId) throws Exception {
        String objectName = uploadAvatar(file, userId);
        // 阿里云 OSS 的 URL 格式：https://{bucket-name}.{endpoint}/{object-name}
        // 如果配置了 url-prefix，则使用配置的前缀
        String urlPrefix = urlPrefix();
        if (urlPrefix != null && !urlPrefix.isEmpty()) {
            // 确保 urlPrefix 不以 / 结尾，objectName 不以 / 开头
            String prefix = urlPrefix.endsWith("/") ? urlPrefix.substring(0, urlPrefix.length() - 1) : urlPrefix;
            String object = objectName.startsWith("/") ? objectName : "/" + objectName;
            return prefix + object;
        } else {
            // 默认格式：https://{bucket-name}.{endpoint}/{object-name}
            String endpointWithoutProtocol = endpoint().replace("https://", "").replace("http://", "");
            return "https://" + bucketName() + "." + endpointWithoutProtocol + "/" + objectName;
        }
    }

    /**
     * 上传头像文件并返回临时访问链接
     * @param file 头像文件
     * @param userId 用户ID
     * @return 临时访问链接
     */
    public String uploadAvatarWithPresignedUrl(MultipartFile file, String userId) throws Exception {
        String objectName = uploadAvatar(file, userId);
        return getPresignedUrl(objectName);
    }

    /**
     * 删除用户头像
     * @param userId 用户ID（仅数字，防路径穿越）
     */
    public void deleteAvatar(String userId) throws Exception {
        validateUserId(userId);
        String prefix = "avatar/" + userId;
        ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
                .withBucketName(bucketName())
                .withPrefix(prefix);

        ListObjectsV2Result result = ossClient.listObjectsV2(listObjectsRequest);

        for (OSSObjectSummary objectSummary : result.getObjectSummaries()) {
            ossClient.deleteObject(bucketName(), objectSummary.getKey());
        }
    }

    /**
     * 根据对象路径生成永久访问 URL（与 uploadAvatarWithUrl 中逻辑一致，私有桶下直接访问可能 403，需配合预签名使用）
     * @param objectName 对象路径，如 avatar/113.webp
     * @return 永久 URL，用于存库；若桶为私有，前端展示时应使用 getPresignedUrl 得到的链接
     */
    public String getPermanentUrl(String objectName) {
        if (!StringUtils.hasText(objectName) || objectName.contains("..") || objectName.startsWith("/")) {
            throw new IllegalArgumentException("无效的对象路径");
        }
        if (!StringUtils.hasText(endpoint())) {
            throw new IllegalStateException("OSS endpoint 未配置，请设置环境变量 OSS_ENDPOINT");
        }
        if (!StringUtils.hasText(bucketName())) {
            throw new IllegalStateException("OSS bucketName 未配置，请设置环境变量 OSS_BUCKET_NAME");
        }
        String urlPrefix = urlPrefix();
        if (urlPrefix != null && !urlPrefix.isEmpty()) {
            String prefix = urlPrefix.endsWith("/") ? urlPrefix.substring(0, urlPrefix.length() - 1) : urlPrefix;
            String object = objectName.startsWith("/") ? objectName : "/" + objectName;
            return prefix + object;
        }
        String endpointWithoutProtocol = endpoint().replace("https://", "").replace("http://", "");
        return "https://" + bucketName() + "." + endpointWithoutProtocol + "/" + objectName;
    }

    /**
     * 生成文件临时访问链接（私有桶可用，有效期默认7天）
     * @param objectName 文件路径（禁止 .. 等路径穿越）
     * @return 临时URL
     */
    public String getPresignedUrl(String objectName) throws Exception {
        if (!StringUtils.hasText(objectName) || objectName.contains("..") || objectName.startsWith("/")) {
            throw new IllegalArgumentException("无效的对象路径");
        }
        if (!StringUtils.hasText(endpoint())) {
            throw new IllegalStateException("OSS endpoint 未配置，请设置环境变量 OSS_ENDPOINT");
        }
        if (!StringUtils.hasText(bucketName())) {
            throw new IllegalStateException("OSS bucketName 未配置，请设置环境变量 OSS_BUCKET_NAME");
        }
        if (!StringUtils.hasText(accessKeyId())) {
            throw new IllegalStateException("OSS accessKeyId 未配置，请设置环境变量 OSS_ACCESS_KEY_ID");
        }
        if (!StringUtils.hasText(accessKeySecret())) {
            throw new IllegalStateException("OSS accessKeySecret 未配置，请设置环境变量 OSS_ACCESS_KEY_SECRET");
        }
        String resolvedObjectName = resolveAvatarObjectName(objectName);
        Date expiration = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L);
        URL url = ossClient.generatePresignedUrl(bucketName(), resolvedObjectName, expiration);
        return ensureHttpsUrl(url.toString());
    }

    /** 预签名 URL 统一使用 HTTPS，避免私有桶链接在浏览器/小程序中被 http 策略拦截 */
    private static String ensureHttpsUrl(String url) {
        if (url != null && url.startsWith("http://")) {
            return "https://" + url.substring("http://".length());
        }
        return url;
    }

    /**
     * 通用文件上传方法
     * @param file 上传文件
     * @param objectName 对象路径
     * @throws Exception 上传失败时抛出异常
     */
    public void uploadFile(MultipartFile file, String objectName) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (!StringUtils.hasText(objectName) || objectName.contains("..") || objectName.startsWith("/")) {
            throw new IllegalArgumentException("无效的对象路径");
        }
        ObjectMetadata metadata = new ObjectMetadata();
        String contentType = file.getContentType();
        metadata.setContentType(StringUtils.hasText(contentType) ? contentType : "application/octet-stream");
        metadata.setContentLength(file.getSize());
        PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucketName(),
                objectName,
                file.getInputStream()
        );
        putObjectRequest.setMetadata(metadata);
        ossClient.putObject(putObjectRequest);
    }

    /**
     * 按对象路径删除单个 OSS 对象。
     *
     * @param objectName 对象路径
     */
    public void deleteObject(String objectName) throws Exception {
        if (!StringUtils.hasText(objectName) || objectName.contains("..") || objectName.startsWith("/")) {
            throw new IllegalArgumentException("无效的对象路径");
        }
        ossClient.deleteObject(bucketName(), objectName);
    }

    /**
     * 下载 OSS 对象字节内容。
     *
     * @param objectName 对象路径
     * @return 文件字节
     */
    public byte[] downloadObject(String objectName) throws Exception {
        if (!StringUtils.hasText(objectName) || objectName.contains("..") || objectName.startsWith("/")) {
            throw new IllegalArgumentException("无效的对象路径");
        }
        try (OSSObject ossObject = ossClient.getObject(bucketName(), objectName);
             java.io.InputStream inputStream = ossObject.getObjectContent()) {
            return inputStream.readAllBytes();
        }
    }
}

