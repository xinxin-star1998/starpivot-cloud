package cn.org.starpivot.common.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 内部服务鉴权 Spring Boot 自动配置类。
 * <p>
 * 将 {@link InternalServiceProperties} 注册为配置 Bean，供
 * {@link cn.org.starpivot.common.filter.InternalServiceAuthFilter} 读取服务间共享 Token。
 * <ul>
 *   <li>{@link AutoConfiguration} — 随 {@code starpivot-common} 自动配置链加载，无需业务模块手动 {@code @Import}</li>
 *   <li>{@link EnableConfigurationProperties} — 启用 {@link InternalServiceProperties} 的属性绑定
 *       （前缀 {@code starpivot.internal}）</li>
 * </ul>
 *
 * @see InternalServiceProperties
 * @see cn.org.starpivot.common.filter.InternalServiceAuthFilter
 */
@AutoConfiguration
@EnableConfigurationProperties(InternalServiceProperties.class)
public class InternalServiceAutoConfiguration {
}
