package cn.org.starpivot.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关鉴权相关配置属性类。
 * <p>
 * 绑定 {@code starpivot.gateway} 前缀的配置项，供 {@link cn.org.starpivot.gateway.filter.AuthGlobalFilter} 读取白名单路径。
 * <ul>
 *   <li>{@link ConfigurationProperties} — 从 application.yml / Nacos 加载 {@code starpivot.gateway.*} 属性</li>
 *   <li>{@link Data} — Lombok 生成 getter/setter，支持配置覆盖默认值</li>
 * </ul>
 */
@Data
@ConfigurationProperties(prefix = "starpivot.gateway")
public class GatewayAuthProperties {

    /**
     * 无需 JWT 鉴权的路径列表（Ant 风格），与网关收到的外部路径一致，通常为 {@code /api/v1/auth/*}。
     */
    private List<String> whitelist = new ArrayList<>(List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/register",
            "/api/v1/auth/logout",
            "/api/v1/auth/captcha",
            "/api/v1/auth/captcha/**",
            "/api/v1/auth/register/enabled",
            "/api/v1/router/dynamic-routes",
            "/**/actuator/**",
            "/**/doc.html",
            "/**/swagger-ui/**",
            "/**/v3/api-docs/**",
            "/**/webjars/**"
    ));
}
