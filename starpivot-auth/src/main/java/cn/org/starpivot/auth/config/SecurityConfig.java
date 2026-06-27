package cn.org.starpivot.auth.config;

import cn.org.starpivot.common.filter.MicroserviceAuthenticationFilter;
import cn.org.starpivot.common.security.MicroserviceSecuritySupport;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 安全配置类。
 * <p>
 * 配置认证服务的无状态 JWT 安全策略，放行登录、注册、验证码等公开接口，其余请求需认证。
 * </p>
 * <ul>
 *   <li>{@link Configuration} — 声明为 Spring 配置类</li>
 *   <li>{@link EnableWebSecurity} — 启用 Web 层 Spring Security 支持</li>
 *   <li>{@link EnableMethodSecurity} — 启用方法级安全注解（如 {@code @PreAuthorize}）</li>
 *   <li>{@link RequiredArgsConstructor} — Lombok 生成含 {@code final} 字段的构造器，用于依赖注入</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final MicroserviceAuthenticationFilter microserviceAuthenticationFilter;
    private final AuthenticationEntryPoint unauthorizedEntryPoint;

    /**
     * 构建认证服务安全过滤器链。
     * <p>
     * 禁用表单登录、HTTP Basic 及默认登出；通过 {@link MicroserviceSecuritySupport} 应用 JWT 过滤器，
     * 并对 OPTIONS、登录/注册/验证码、Swagger、Actuator 等路径放行。
     * </p>
     *
     * @param http {@link HttpSecurity} 构建器
     * @return 配置完成的 {@link SecurityFilterChain}
     * @throws Exception 安全配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable());

        MicroserviceSecuritySupport.applyStatelessJwtSecurity(
                http,
                microserviceAuthenticationFilter,
                unauthorizedEntryPoint,
                auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/login", "/refresh", "/register", "/logout",
                                "/forgot-password", "/captcha/verify").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/register/enabled", "/forgot-password/enabled",
                                "/user/info", "/userinfo").permitAll()
                        .requestMatchers(
                                "/captcha", "/captcha/**",
                                "/actuator/**",
                                "/doc.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
