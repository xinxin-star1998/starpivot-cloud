package cn.org.starpivot.common.filter;

import cn.org.starpivot.common.config.InternalServiceProperties;
import cn.org.starpivot.common.security.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 校验 /internal/** 请求的服务间 Token，防止直连微服务端口绕过网关。
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class InternalServiceAuthFilter extends OncePerRequestFilter {

    private final InternalServiceProperties internalServiceProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path == null || !path.startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String configuredToken = internalServiceProperties.getToken();
        if (!StringUtils.hasText(configuredToken)) {
            log.debug("Internal service token not configured, skipping validation for {}", request.getServletPath());
            filterChain.doFilter(request, response);
            return;
        }

        String requestToken = request.getHeader(SecurityConstants.INTERNAL_TOKEN_HEADER);
        if (configuredToken.equals(requestToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        log.warn("Rejected internal request without valid token: {} {}", request.getMethod(), request.getServletPath());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"code\":401,\"message\":\"无效的内部服务凭证\"}");
    }
}
