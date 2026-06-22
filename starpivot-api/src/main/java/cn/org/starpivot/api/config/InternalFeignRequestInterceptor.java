package cn.org.starpivot.api.config;

import cn.org.starpivot.common.config.InternalServiceProperties;
import cn.org.starpivot.common.security.SecurityConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * Feign 调用 /internal/** 时自动附加服务间 Token。
 * 由 {@link InternalFeignAutoConfiguration} 在 OpenFeign 可用时注册，避免无 Feign 依赖的服务启动失败。
 */
@RequiredArgsConstructor
public class InternalFeignRequestInterceptor implements RequestInterceptor {

    private final InternalServiceProperties internalServiceProperties;

    @Override
    public void apply(RequestTemplate template) {
        String token = internalServiceProperties.getToken();
        if (StringUtils.hasText(token) && template.path() != null && template.path().contains("/internal/")) {
            template.header(SecurityConstants.INTERNAL_TOKEN_HEADER, token);
        }
    }
}
