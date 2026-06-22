package cn.org.starpivot.common.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 自动注册内部服务鉴权配置，供 {@link cn.org.starpivot.common.filter.InternalServiceAuthFilter} 使用。
 */
@AutoConfiguration
@EnableConfigurationProperties(InternalServiceProperties.class)
public class InternalServiceAutoConfiguration {
}
