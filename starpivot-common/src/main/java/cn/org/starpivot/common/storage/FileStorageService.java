package cn.org.starpivot.common.storage;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * 文件存储服务统一接口
 * 支持多种存储方式（OSS 等）的统一抽象
 * <p>
 * 架构分层：
 * - 业务接口层：AvatarController、GoodsImageController 等
 * - 公共服务层：FileStorageService（统一文件处理逻辑）
 * - 存储驱动层：OssFileStorageService（具体存储实现）
 * </p>
 *
 * @author stardust
 */
public interface FileStorageService {

    /**
     * 上传头像文件
     *
     * @param file 头像文件
     * @param userId 用户ID
     * @return 头像文件在存储桶中的路径（如：avatar/123.png）
     * @throws Exception 上传失败时抛出异常
     */
    String uploadAvatar(MultipartFile file, String userId) throws Exception;

    /**
     * 上传头像文件并返回完整访问URL（适用于公共桶场景）
     *
     * @param file 头像文件
     * @param userId 用户ID
     * @return 完整的头像访问URL
     * @throws Exception 上传失败时抛出异常
     */
    String uploadAvatarWithUrl(MultipartFile file, String userId) throws Exception;

    /**
     * 上传头像文件并返回临时访问链接（适用于私有桶场景）
     *
     * @param file 头像文件
     * @param userId 用户ID
     * @return 临时访问链接（默认7天有效期）
     * @throws Exception 上传失败时抛出异常
     */
    String uploadAvatarWithPresignedUrl(MultipartFile file, String userId) throws Exception;

    /**
     * 删除用户头像
     *
     * @param userId 用户ID
     * @throws Exception 删除失败时抛出异常
     */
    void deleteAvatar(String userId) throws Exception;

    /**
     * 生成文件临时访问链接（私有桶专用）
     *
     * @param objectName 文件路径（如：avatar/123.png）
     * @return 临时访问链接（默认7天有效期）
     * @throws Exception 生成失败时抛出异常
     */
    String getPresignedUrl(String objectName) throws Exception;

    /**
     * 生成文件永久访问URL（公共桶专用）
     *
     * @param objectName 文件路径（如：avatar/123.png）
     * @return 永久访问URL
     */
    String getPermanentUrl(String objectName);

    /**
     * 上传富文本编辑器图片并返回可写入 HTML 的访问 URL（对象路径：editor/...）。
     * 私有存储桶场景一般为预签名 URL，便于浏览器直接加载；永久直链在私有桶下会 403。
     *
     * @param file 图片文件
     * @return 图片访问 URL
     * @throws Exception 上传失败时抛出异常
     */
    String uploadEditorImageWithUrl(MultipartFile file) throws Exception;

    /**
     * 上传头像并返回完整信息（包含永久URL和预签名URL）
     *
     * @param file 头像文件
     * @param userId 用户ID
     * @return 包含 avatarUrl 和 presignedUrl 的 Map
     * @throws Exception 上传失败时抛出异常
     */
    default Map<String, String> uploadAvatarWithFullInfo(MultipartFile file, String userId) throws Exception {
        String objectName = uploadAvatar(file, userId);
        String avatarUrl = getPermanentUrl(objectName);
        String presignedUrl = getPresignedUrl(objectName);

        return Map.of(
            "avatarUrl", avatarUrl,
            "presignedUrl", presignedUrl
        );
    }

    /**
     * 通用文件上传方法
     * <p>
     * 统一封装文件接收、格式校验、转存、返回结果的全流程。
     * 对外暴露两个独立接口：
     * - /api/user/avatar/upload 头像上传
     * - /api/goods/image/upload 商品图片上传
     * 各接口自行处理业务权限校验和后置逻辑。
     * </p>
     *
     * @param file          上传文件
     * @param category      业务分类（如：avatar, goods, editor, attachment）
     * @param businessId    业务ID（可选，用于分目录存储）
     * @param allowedTypes  允许的文件类型（可选，如：image/png, image/jpeg）
     * @param maxSize       最大文件大小（字节，可选）
     * @return 上传结果封装（包含对象路径、永久URL、预签名URL）
     * @throws Exception 上传失败时抛出异常
     */
    default UploadResult uploadFile(MultipartFile file, String category, String businessId,
                                   String[] allowedTypes, Long maxSize) throws Exception {
        validateFile(file, allowedTypes, maxSize);

        String objectName = generateObjectName(category, businessId, file.getOriginalFilename(), allowedTypes);

        uploadFileInternal(file, objectName);

        // 返回结果
        return UploadResult.builder()
                .objectName(objectName)
                .permanentUrl(getPermanentUrl(objectName))
                .presignedUrl(getPresignedUrl(objectName))
                .build();
    }

    /**
     * 内部方法：生成对象路径
     */
    default String generateObjectName(String category, String businessId, String originalFilename,
                                      String[] allowedTypes) {
        String suffix = getFileSuffix(originalFilename, allowedTypes);
        String day = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        if (org.springframework.util.StringUtils.hasText(businessId)) {
            return String.format("%s/%s/%s/%s%s",
                    category, day, businessId, UUID.randomUUID(), suffix);
        } else {
            return String.format("%s/%s/%s%s",
                    category, day, UUID.randomUUID(), suffix);
        }
    }

    /**
     * 内部方法：文件校验
     */
    default void validateFile(MultipartFile file, String[] allowedTypes, Long maxSize) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 文件大小校验
        if (maxSize != null && file.getSize() > maxSize) {
            throw new IllegalArgumentException(String.format("文件大小不能超过 %dMB", maxSize / 1024 / 1024));
        }

        // 文件类型校验
        if (allowedTypes != null && allowedTypes.length > 0) {
            String contentType = file.getContentType();
            boolean allowed = false;
            for (String type : allowedTypes) {
                if (type.equals(contentType)) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                throw new IllegalArgumentException("不支持的文件类型，仅支持：" + String.join(", ", allowedTypes));
            }
        }
    }

    /** 图片类上传允许的文件后缀 */
    String[] IMAGE_SUFFIXES = {".jpeg", ".jpg", ".png", ".gif", ".webp"};

    /**
     * 内部方法：获取文件后缀；图片上传场景仅保留白名单后缀，防止恶意扩展名
     */
    default String getFileSuffix(String filename, String[] allowedTypes) {
        if (!org.springframework.util.StringUtils.hasText(filename) || !filename.contains(".")) {
            return ".png";
        }
        String suffix = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        if (isImageUpload(allowedTypes) && !isAllowedImageSuffix(suffix)) {
            return ".png";
        }
        return suffix;
    }

    default boolean isImageUpload(String[] allowedTypes) {
        if (allowedTypes == null) {
            return false;
        }
        for (String type : allowedTypes) {
            if (type != null && type.startsWith("image/")) {
                return true;
            }
        }
        return false;
    }

    default boolean isAllowedImageSuffix(String suffix) {
        for (String allowed : IMAGE_SUFFIXES) {
            if (allowed.equals(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 子类必须实现的内部上传方法
     *
     * @param file       上传文件
     * @param objectName 对象路径
     * @throws Exception 上传失败时抛出异常
     */
    void uploadFileInternal(MultipartFile file, String objectName) throws Exception;

    /**
     * 删除存储对象（Phase 2 物理清理预留，文件中心逻辑删除默认不调用）。
     *
     * @param objectName 对象路径
     */
    default void deleteObject(String objectName) throws Exception {
        // 默认空实现，子类按需覆盖
    }
}
