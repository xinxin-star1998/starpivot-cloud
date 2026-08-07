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
 * <p>
 * 路径前缀通过 {@code starpivot.api.version} 配置（默认 {@code v1}），运行时自动拼接为 {@code /api/v1/...}。
 */
@Data
@ConfigurationProperties(prefix = "starpivot.gateway")
public class GatewayAuthProperties {

    /**
     * API 版本前缀，与 Nacos {@code starpivot.api.version} 保持一致。
     */
    private String apiVersion = "v1";

    /**
     * 无需 JWT 鉴权的路径列表（Ant 风格），与网关收到的外部路径一致。
     * <p>
     * 默认值使用 {@code apiVersion} 拼接，保证版本升级时一处修改全局生效。
     */
    private List<String> whitelist;

    /**
     * 懒初始化白名单：首次访问时若未通过外部配置覆盖，则使用基于 {@code apiVersion} 的默认列表。
     */
    public List<String> getWhitelist() {
        if (whitelist == null) {
            String v = "/" + apiVersion + "/";
            whitelist = new ArrayList<>(List.of(
                    "/api" + v + "auth/login",
                    "/api" + v + "auth/refresh",
                    "/api" + v + "auth/register",
                    "/api" + v + "auth/logout",
                    "/api" + v + "auth/captcha",
                    "/api" + v + "auth/captcha/**",
                    "/api" + v + "auth/register/enabled",
                    "/api" + v + "auth/forgot-password",
                    "/api" + v + "auth/forgot-password/**",
                    "/api" + v + "portal/home/**",
                    "/api" + v + "portal/product/**",
                    "/api" + v + "portal/subject/**",
                    "/api" + v + "portal/seckill/**",
                    "/api" + v + "portal/comment/commentPageList",
                    "/api" + v + "portal/comment/can-comment/**",
                    "/api" + v + "portal/comment/summary/**",
                    "/api" + v + "portal/region/**",
                    "/api" + v + "portal/member/register",
                    "/api" + v + "portal/member/login",
                    "/api" + v + "portal/auth/config",
                    "/api" + v + "portal/auth/sms/**",
                    "/api" + v + "portal/auth/login/password",
                    "/api" + v + "portal/auth/wechat/**",
                    "/api" + v + "portal/image/presigned-urls",
                    "/api" + v + "portal/pay/alipay/notify",
                    "/api" + v + "portal/pay/wx/notify",
                    "/api" + v + "portal/pay/wx/refund/notify",
                    "/**/actuator/health",
                    "/**/actuator/health/**"
            ));
        }
        return whitelist;
    }
}
