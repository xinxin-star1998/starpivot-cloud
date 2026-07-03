package cn.org.starpivot.common.security;

import cn.org.starpivot.common.filter.MicroserviceAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 微服务 {@code SecurityFilterChain} 公共配置片段。
 * <p>
 * 统一关闭 CSRF、启用无状态 Session，并挂载 {@link MicroserviceAuthenticationFilter}。
 * </p>
 */
public final class MicroserviceSecuritySupport {

    private MicroserviceSecuritySupport() {
    }

    /**
     * 应用 JWT 无状态安全配置。
     *
     * @param http                   {@link HttpSecurity} 构建器
     * @param filter                 JWT 认证过滤器
     * @param unauthorizedEntryPoint 未认证时的响应入口
     * @param customizer             各服务自定义的 {@code authorizeHttpRequests} 规则
     */
    public static void applyStatelessJwtSecurity(
            HttpSecurity http,
            MicroserviceAuthenticationFilter filter,
            AuthenticationEntryPoint unauthorizedEntryPoint,
            Customizer customizer) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedEntryPoint))
                .authorizeHttpRequests(customizer::customize)
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * 注册基础设施类公开端点：health/info 匿名，其余 Actuator 与 OpenAPI 按策略放行/鉴权。
     */
    public static void permitInfrastructureEndpoints(
            org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(MicroserviceEndpointPatterns.PUBLIC_ACTUATOR).permitAll()
                .requestMatchers(MicroserviceEndpointPatterns.OPENAPI).permitAll()
                .requestMatchers("/actuator/**").authenticated();
    }

    /** 各微服务声明 URL 授权规则的函数式接口 */
    @FunctionalInterface
    public interface Customizer {

        /**
         * 配置 {@code authorizeHttpRequests} 匹配规则。
         *
         * @param auth Spring Security 授权注册器
         */
        void customize(
                org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth);
    }
}
