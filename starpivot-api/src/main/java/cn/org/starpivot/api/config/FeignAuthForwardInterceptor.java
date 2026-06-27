package cn.org.starpivot.api.config;

import cn.org.starpivot.common.security.SecurityConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 将当前 HTTP 请求的 Authorization 头转发到 Feign 调用，便于下游服务识别登录用户。
 */
public class FeignAuthForwardInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        if (request == null) {
            return;
        }
        String authorization = request.getHeader(SecurityConstants.TOKEN_HEADER);
        if (StringUtils.hasText(authorization)) {
            template.header(SecurityConstants.TOKEN_HEADER, authorization);
        }
    }
}
