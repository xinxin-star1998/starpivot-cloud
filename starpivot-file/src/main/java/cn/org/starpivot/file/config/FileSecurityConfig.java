package cn.org.starpivot.file.config;

import cn.org.starpivot.common.filter.MicroserviceAuthenticationFilter;
import cn.org.starpivot.common.security.MicroserviceSecuritySupport;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 文件服务 Spring Security 配置（Servlet 栈）。
 * <p>
 * 本模块为 Spring MVC 微服务，安全链基于 Servlet {@link SecurityFilterChain}，
 * 与网关 WebFlux {@link org.springframework.cloud.gateway.filter.GlobalFilter} 不同。
 * 复用 {@link MicroserviceSecuritySupport} 统一无状态 JWT 鉴权，并放行文档与 Actuator 端点。
 * <ul>
 *   <li>{@link Configuration} — 声明 Spring 配置类</li>
 *   <li>{@link EnableWebSecurity} — 启用 Spring Security Web 支持</li>
 *   <li>{@link RequiredArgsConstructor} — 注入 {@link MicroserviceAuthenticationFilter} 与未授权入口</li>
 * </ul>
 *
 * @see MicroserviceAuthenticationFilter
 * @see MicroserviceSecuritySupport
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class FileSecurityConfig {

    private final MicroserviceAuthenticationFilter microserviceAuthenticationFilter;
    private final AuthenticationEntryPoint unauthorizedEntryPoint;

    /**
     * 构建文件服务的 {@link SecurityFilterChain}：Actuator/Swagger 匿名访问，其余需认证。
     *
     * @param http Spring Security HTTP 配置构建器（Servlet 上下文）
     * @return 配置完成的过滤器链 Bean
     * @throws Exception 安全配置构建失败时抛出
     */
    @Bean
    public SecurityFilterChain fileSecurityFilterChain(HttpSecurity http) throws Exception {
        MicroserviceSecuritySupport.applyStatelessJwtSecurity(
                http,
                microserviceAuthenticationFilter,
                unauthorizedEntryPoint,
                auth -> {
                    auth.requestMatchers("/internal/**").permitAll();
                    MicroserviceSecuritySupport.permitInfrastructureEndpoints(auth);
                    auth.anyRequest().authenticated();
                });
        return http.build();
    }
}
