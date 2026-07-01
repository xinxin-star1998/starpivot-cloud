package cn.org.starpivot.monitor.config;

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
 * 监控服务 Spring Security 配置。
 * <p>
 * {@link Configuration}：声明为配置类；
 * {@link EnableWebSecurity}：启用 Web 安全；
 * {@link EnableMethodSecurity}：启用方法级权限注解（如 {@link org.springframework.security.access.prepost.PreAuthorize}）；
 * {@link RequiredArgsConstructor}：为 final 依赖生成构造器注入。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class MonitorSecurityConfig {

    private final MicroserviceAuthenticationFilter microserviceAuthenticationFilter;
    private final AuthenticationEntryPoint unauthorizedEntryPoint;

    /**
     * 配置无状态 JWT 安全过滤链，放行监控与文档端点。
     *
     * @param http Spring Security 构建器
     * @return 已构建的安全过滤链
     * @throws Exception 配置失败时抛出
     */
    @Bean
    public SecurityFilterChain monitorSecurityFilterChain(HttpSecurity http) throws Exception {
        MicroserviceSecuritySupport.applyStatelessJwtSecurity(
                http,
                microserviceAuthenticationFilter,
                unauthorizedEntryPoint,
                auth -> auth
                        .requestMatchers(
                                "/actuator/**",
                                "/druid/**",
                                "/doc.html",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
