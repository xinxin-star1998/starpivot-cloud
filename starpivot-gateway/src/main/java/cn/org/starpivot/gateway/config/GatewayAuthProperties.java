package cn.org.starpivot.gateway.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;



/**

 * 网关鉴权相关配置属性类。

 * <p>

 * 绑定 {@code starpivot.gateway} 前缀的配置项，供 {@link cn.org.starpivot.gateway.filter.AuthGlobalFilter} 读取白名单路径。

 * 默认值应与 {@code application.yml} / Nacos 中 {@code starpivot.gateway.whitelist} 保持一致，避免仅依赖 Java 默认值时漏放行。

 */

@Data

@ConfigurationProperties(prefix = "starpivot.gateway")

public class GatewayAuthProperties {



    /**

     * 无需 JWT 鉴权的路径列表（Ant 风格），与网关收到的外部路径一致。

     */

    private List<String> whitelist = new ArrayList<>(List.of(

            "/api/v1/auth/login",

            "/api/v1/auth/refresh",

            "/api/v1/auth/register",

            "/api/v1/auth/logout",

            "/api/v1/auth/captcha",

            "/api/v1/auth/captcha/**",

            "/api/v1/auth/register/enabled",

            "/api/v1/auth/forgot-password",

            "/api/v1/auth/forgot-password/**",

            "/api/v1/router/dynamic-routes",

            "/api/v1/portal/home/**",

            "/api/v1/portal/product/**",

            "/api/v1/portal/subject/**",

            "/api/v1/portal/seckill/**",

            "/api/v1/portal/comment/commentPageList",

            "/api/v1/portal/comment/can-comment/**",

            "/api/v1/portal/comment/summary/**",

            "/api/v1/portal/region/**",

            "/api/v1/portal/member/register",

            "/api/v1/portal/member/login",

            "/api/v1/portal/auth/config",

            "/api/v1/portal/auth/sms/**",

            "/api/v1/portal/auth/login/password",

            "/api/v1/portal/auth/wechat/**",

            "/api/v1/portal/pay/alipay/notify",

            "/**/actuator/**",

            "/**/doc.html",

            "/**/v3/api-docs/**",

            "/**/webjars/**"

    ));

}

