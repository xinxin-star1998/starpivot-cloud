package cn.org.starpivot.common.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传统一响应 VO
 * <p>
 * objectName：存库字段（OSS 对象路径）<br>
 * displayUrl：前端展示用 URL（私有桶为预签名 URL，公共桶为永久 URL）
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadVO {

    /** OSS 对象路径，写入数据库 */
    private String objectName;

    /** 前端 img 标签 src，优先预签名 URL */
    private String displayUrl;

    /** 永久访问 URL（公共桶） */
    private String permanentUrl;

    /** 预签名临时 URL（私有桶） */
    private String presignedUrl;

    public static FileUploadVO from(UploadResult result) {
        if (result == null) {
            return null;
        }
        return FileUploadVO.builder()
                .objectName(result.getObjectName())
                .displayUrl(result.getDisplayUrl())
                .permanentUrl(result.getPermanentUrl())
                .presignedUrl(result.getPresignedUrl())
                .build();
    }
}

