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
public class MallSecurityConfig {

    private final MicroserviceAuthenticationFilter microserviceAuthenticationFilter;
    private final AuthenticationEntryPoint unauthorizedEntryPoint;

    @Bean
    public SecurityFilterChain mallSecurityFilterChain(HttpSecurity http) throws Exception {
        MicroserviceSecuritySupport.applyStatelessJwtSecurity(
                http,
                microserviceAuthenticationFilter,
                unauthorizedEntryPoint,
                auth -> auth
                        .requestMatchers(
                                "/internal/**",
                                "/actuator/**",
                                "/doc.html",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/health",
                                "/portal/home/**",
                                "/portal/product/**",
                                "/portal/subject/**",
                                "/portal/seckill/**",
                                "/portal/comment/commentPageList",
                                "/portal/comment/can-comment/**",
                                "/portal/comment/summary/**",
                                "/portal/region/**",
                                "/portal/member/register",
                                "/portal/member/login",
                                "/portal/auth/config",
                                "/portal/auth/sms/**",
                                "/portal/auth/login/password",
                                "/portal/auth/wechat/**",
                                "/portal/pay/alipay/notify",
                                "/portal/pay/wx/notify"
                        ).permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
