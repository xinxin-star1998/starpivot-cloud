package cn.org.starpivot.common.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传结果封装类
 * 统一封装文件上传后的关键信息，便于各业务场景使用
 *
 * @author stardust
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResult {

    /**
     * 对象路径，用于存储到数据库（如：goods/2026/05/29/123/uuid.png）
     */
    private String objectName;

    /**
     * 永久访问URL，适用于公共桶场景
     */
    private String permanentUrl;

    /**
     * 预签名临时访问URL，适用于私有桶场景（默认7天有效期）
     */
    private String presignedUrl;

    /**
     * 便捷方法：获取适合存储的路径
     * 对于私有桶返回 objectName，对于公共桶返回 permanentUrl
     *
     * @return 存储用路径
     */
    public String getStoragePath() {
        return objectName;
    }

    /**
     * 便捷方法：获取适合展示的URL
     * 优先返回预签名URL（私有桶场景），其次返回永久URL（公共桶场景）
     *
     * @return 展示用URL
     */
    public String getDisplayUrl() {
        if (presignedUrl != null && !presignedUrl.isEmpty()) {
            return presignedUrl;
        }
        return permanentUrl;
    }
}
