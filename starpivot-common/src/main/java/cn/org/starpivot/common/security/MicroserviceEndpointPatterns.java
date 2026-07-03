package cn.org.starpivot.common.security;

/**
 * 各微服务 Security 链中可复用的公开端点路径。
 * <p>
 * Actuator 仅放行 health/info；metrics、prometheus 等敏感端点需认证后访问。
 */
public final class MicroserviceEndpointPatterns {

    /** 无需登录即可访问的 Actuator 端点 */
    public static final String[] PUBLIC_ACTUATOR = {
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info"
    };

    /** Knife4j / OpenAPI 文档 */
    public static final String[] OPENAPI = {
            "/doc.html",
            "/v3/api-docs/**",
            "/webjars/**"
    };

    /** 各服务自定义健康检查（非 Actuator） */
    public static final String HEALTH = "/health";

    private MicroserviceEndpointPatterns() {
    }
}
