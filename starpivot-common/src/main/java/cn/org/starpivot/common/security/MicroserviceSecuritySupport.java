package cn.org.starpivot.common.security;

import cn.org.starpivot.common.filter.MicroserviceAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 微服务 SecurityFilterChain 公共配置片段。
 */
public final class MicroserviceSecuritySupport {

    private MicroserviceSecuritySupport() {
    }

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

    @FunctionalInterface
    public interface Customizer {
        void customize(
                org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth);
    }
}
