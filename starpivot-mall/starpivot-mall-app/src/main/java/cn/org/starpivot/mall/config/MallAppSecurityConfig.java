package cn.org.starpivot.mall.config;

import cn.org.starpivot.common.filter.MicroserviceAuthenticationFilter;
import cn.org.starpivot.common.security.MicroserviceSecuritySupport;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 商城静态资源 BFF 安全配置：仅开放 /local-storage/**。
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class MallAppSecurityConfig {

    private final MicroserviceAuthenticationFilter microserviceAuthenticationFilter;
    private final AuthenticationEntryPoint unauthorizedEntryPoint;

    @Bean
    public SecurityFilterChain mallAppSecurityFilterChain(HttpSecurity http) throws Exception {
        MicroserviceSecuritySupport.applyStatelessJwtSecurity(
                http,
                microserviceAuthenticationFilter,
                unauthorizedEntryPoint,
                auth -> {
                    auth.requestMatchers(HttpMethod.GET, "/local-storage/**").permitAll();
                    MicroserviceSecuritySupport.permitInfrastructureEndpoints(auth);
                    auth.anyRequest().denyAll();
                });
        return http.build();
    }
}
