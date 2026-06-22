package cn.org.starpivot.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * 微服务通用 Spring Security 辅助 Bean 配置类。
 * <p>
 * 提供密码编码器与未认证（401）JSON 响应入口，供各 Servlet 微服务的 Security 链引用。
 * <ul>
 *   <li>{@link Configuration} — 标准 Spring 配置类，由组件扫描或显式导入加载</li>
 *   <li>{@link ConditionalOnWebApplication}（{@code type = SERVLET}）—
 *       仅在 Servlet Web 应用注册，Reactive Gateway 不加载</li>
 * </ul>
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityBeansConfiguration {

    /** 未登录/未授权时写入响应体的 JSON 字符串 */
    public static final String UNAUTHORIZED_JSON = "{\"code\":401,\"message\":\"未授权，请先登录\"}";

    /**
     * 注册 BCrypt 密码编码器。
     *
     * @return 用于用户密码哈希与校验的 {@link PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 注册 Spring Security 认证入口点。
     * <p>
     * 当请求未携带有效凭证访问受保护资源时，返回 HTTP 401 及 {@link #UNAUTHORIZED_JSON}。
     *
     * @return 写入 JSON 401 响应的 {@link AuthenticationEntryPoint}
     */
    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(UNAUTHORIZED_JSON);
        };
    }
}
