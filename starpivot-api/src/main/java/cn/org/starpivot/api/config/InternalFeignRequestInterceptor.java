package cn.org.starpivot.api.config;

import cn.org.starpivot.common.config.InternalServiceProperties;
import cn.org.starpivot.common.observability.TraceIdConstants;
import cn.org.starpivot.common.security.SecurityConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

/**
 * Feign 请求拦截器，用于服务间内部调用鉴权与链路追踪。
 * <p>
 * 实现 {@link RequestInterceptor}，在 Feign 发起请求前：
 * <ul>
 *   <li>将 MDC 中的 TraceId 写入请求头，保持跨服务链路一致</li>
 *   <li>对路径包含 {@code /internal/} 的请求附加内部服务 Token</li>
 * </ul>
 * 由 {@link InternalFeignAutoConfiguration} 在 OpenFeign 可用时注册。
 * <p>
 * 注解说明：
 * <ul>
 *   <li>{@link RequiredArgsConstructor} — Lombok 生成含 {@code internalServiceProperties} 的构造器</li>
 * </ul>
 */
@RequiredArgsConstructor
public class InternalFeignRequestInterceptor implements RequestInterceptor {

    private final InternalServiceProperties internalServiceProperties;

    /**
     * 拦截 Feign 请求，附加 TraceId 与内部服务 Token。
     *
     * @param template Feign 请求模板，可在此修改请求头
     */
    @Override
    public void apply(RequestTemplate template) {
        String traceId = MDC.get(TraceIdConstants.MDC_TRACE_ID_KEY);
        if (StringUtils.hasText(traceId)) {
            template.header(TraceIdConstants.TRACE_ID_HEADER, traceId);
        }

        String token = internalServiceProperties.getToken();
        if (StringUtils.hasText(token) && template.path() != null && template.path().contains("/internal/")) {
            template.header(SecurityConstants.INTERNAL_TOKEN_HEADER, token);
        }
    }
}
