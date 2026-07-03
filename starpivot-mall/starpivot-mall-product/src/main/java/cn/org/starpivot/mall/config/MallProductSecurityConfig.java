package cn.org.starpivot.mall.config;

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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class MallProductSecurityConfig {

    private final MicroserviceAuthenticationFilter microserviceAuthenticationFilter;
    private final AuthenticationEntryPoint unauthorizedEntryPoint;

    @Bean
    public SecurityFilterChain mallProductSecurityFilterChain(HttpSecurity http) throws Exception {
        MicroserviceSecuritySupport.applyStatelessJwtSecurity(
                http,
                microserviceAuthenticationFilter,
                unauthorizedEntryPoint,
                auth -> {
                    auth.requestMatchers("/internal/**").permitAll()
                            .requestMatchers(
                                    "/portal/product/**",
                                    "/portal/comment/commentPageList",
                                    "/portal/comment/can-comment/**",
                                    "/portal/comment/summary/**",
                                    "/portal/image/presigned-urls"
                            ).permitAll();
                    MicroserviceSecuritySupport.permitInfrastructureEndpoints(auth);
                    auth.anyRequest().authenticated();
                });
        return http.build();
    }
}
