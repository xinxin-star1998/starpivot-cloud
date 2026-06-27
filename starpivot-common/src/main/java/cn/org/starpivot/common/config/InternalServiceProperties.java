package cn.org.starpivot.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 微服务间内部调用鉴权配置属性类。
 * <p>
 * 绑定 {@code starpivot.internal.*} 配置项，用于校验 {@code /internal/**} 路径上的服务间 Token，
 * 防止绕过网关直连微服务端口。
 * <ul>
 *   <li>{@link Data} — Lombok 生成 getter/setter、{@code equals}/{@code hashCode}/{@code toString}</li>
 *   <li>{@link ConfigurationProperties}（{@code prefix = "starpivot.internal"}）—
 *       从 application.yml、Nacos 或环境变量注入属性</li>
 * </ul>
 * <p>
 * 生产环境请通过环境变量 {@code INTERNAL_SERVICE_TOKEN} 注入 {@link #token}；
 * 未配置时 {@link cn.org.starpivot.common.filter.InternalServiceAuthFilter} 跳过校验（便于本地开发）。
 *
 * @see cn.org.starpivot.common.filter.InternalServiceAuthFilter
 * @see cn.org.starpivot.common.security.SecurityConstants#INTERNAL_TOKEN_HEADER
 */
@Data
@ConfigurationProperties(prefix = "starpivot.internal")
public class InternalServiceProperties {

    /**
     * 服务间共享密钥。
     * <p>
     * Feign 调用 {@code /internal/**} 时通过请求头携带；为空字符串表示未启用校验。
     */
    private String token = "";

    /**
     * 是否强制要求配置内部 Token（生产环境建议 {@code true}）。
     * <p>
     * 为 {@code true} 且 {@link #token} 为空时，所有 {@code /internal/**} 请求将被拒绝。
     */
    private boolean requireToken = false;
}
