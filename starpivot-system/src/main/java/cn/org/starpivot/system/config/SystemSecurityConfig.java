package cn.org.starpivot.system.config;

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
 * 系统模块 Spring Security 配置类。
 * <p>
 * 基于无状态 JWT 鉴权，放行内部接口、健康检查与 API 文档路径，其余请求需认证。
 * </p>
 * <ul>
 *   <li>{@link Configuration} — 声明为 Spring 配置类</li>
 *   <li>{@link EnableWebSecurity} — 启用 Web 安全过滤器链</li>
 *   <li>{@link EnableMethodSecurity} — 启用方法级权限注解（如 {@code @PreAuthorize}）</li>
 *   <li>{@link RequiredArgsConstructor} — 为 final 依赖字段生成构造器注入</li>
 * </ul>
 *
 * @see MicroserviceSecuritySupport
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SystemSecurityConfig {

    private final MicroserviceAuthenticationFilter microserviceAuthenticationFilter;
    private final AuthenticationEntryPoint unauthorizedEntryPoint;

    /**
     * 构建系统模块安全过滤器链。
     * <p>
     * 通过 {@link MicroserviceSecuritySupport#applyStatelessJwtSecurity} 应用 JWT 鉴权，
     * 并对 {@code /internal/**}、Actuator、Swagger 及 {@code /health} 路径放行。
     * </p>
     *
     * @param http Spring Security HTTP 配置构建器
     * @return 已构建的 {@link SecurityFilterChain}
     * @throws Exception 安全配置异常
     */
    @Bean
    public SecurityFilterChain systemSecurityFilterChain(HttpSecurity http) throws Exception {
        MicroserviceSecuritySupport.applyStatelessJwtSecurity(
                http,
                microserviceAuthenticationFilter,
                unauthorizedEntryPoint,
                auth -> auth
                        .requestMatchers(
                                "/internal/**",
                                "/actuator/**",
                                "/doc.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/health"
                        ).permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
