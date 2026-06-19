package cn.org.starpivot.common.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 阿里云 OSS 客户端单例配置。
 * <p>
 * SDK 文档建议复用 OSS 实例；应用关闭时由 Spring 调用 {@code shutdown()} 释放连接。
 * </p>
 */
@Configuration
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnProperty(prefix = "oss", name = "enabled", havingValue = "true")
public class OssClientConfiguration {

    @Bean(destroyMethod = "shutdown")
    public OSS ossClient(OssProperties properties) {
        return new OSSClientBuilder().build(
                requireText(properties.getEndpoint(), "oss.endpoint / OSS_ENDPOINT"),
                requireText(properties.getAccessKeyId(), "oss.access-key-id / OSS_ACCESS_KEY_ID"),
                requireText(properties.getAccessKeySecret(), "oss.access-key-secret / OSS_ACCESS_KEY_SECRET"));
    }

    private static String requireText(String value, String name) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("未配置 " + name);
        }
        return value;
    }
}
