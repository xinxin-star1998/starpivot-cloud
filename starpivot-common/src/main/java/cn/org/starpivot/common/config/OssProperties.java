package cn.org.starpivot.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云 OSS 配置（对应 application.yml 中 oss.*）
 */
@Data
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    /**
     * 是否启用 OSS（false 时不创建 OSS 客户端 Bean，适用于无对象存储的本地场景）
     */
    private boolean enabled = true;

    private String endpoint = "";

    private String accessKeyId = "";

    private String accessKeySecret = "";

    private String bucketName = "star-pivot";

    /**
     * 自定义 CDN / 公网访问前缀，为空时使用 https://{bucket}.{endpoint}/{object}
     */
    private String urlPrefix = "";
}
