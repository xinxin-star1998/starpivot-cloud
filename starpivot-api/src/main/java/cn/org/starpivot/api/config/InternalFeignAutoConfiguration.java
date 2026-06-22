package cn.org.starpivot.api.config;

import cn.org.starpivot.common.config.InternalServiceProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 内部调用自动配置类。
 * <p>
 * 在 classpath 存在 OpenFeign 时，注册 {@link InternalFeignRequestInterceptor}，
 * 使服务间 Feign 调用 {@code /internal/**} 接口时自动携带内部 Token 与 TraceId。
 * <p>
 * 注解说明：
 * <ul>
 *   <li>{@link Configuration} — 声明为 Spring 配置类</li>
 *   <li>{@link ConditionalOnClass} — 仅当存在 {@code feign.RequestInterceptor} 时生效，避免无 Feign 依赖的服务启动失败</li>
 *   <li>{@link EnableConfigurationProperties} — 启用 {@link InternalServiceProperties} 配置绑定</li>
 * </ul>
 */
@Configuration
@ConditionalOnClass(name = "feign.RequestInterceptor")
@EnableConfigurationProperties(InternalServiceProperties.class)
public class InternalFeignAutoConfiguration {

    /**
     * 注册 Feign 请求拦截器 Bean。
     *
     * @param internalServiceProperties 内部服务鉴权配置（含服务间 Token）
     * @return 自动附加 TraceId 与内部 Token 的拦截器实例
     */
    @Bean
    public InternalFeignRequestInterceptor internalFeignRequestInterceptor(
            InternalServiceProperties internalServiceProperties) {
        return new InternalFeignRequestInterceptor(internalServiceProperties);
    }
}
