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
 * 内部服务接口（{@code /internal/**}）鉴权 Servlet 过滤器。
 * <p>
 * 校验 Feign 等微服务间调用携带的服务间 Token，防止绕过网关直连微服务端口。
 * 继承 {@link OncePerRequestFilter}，每个请求仅校验一次。
 * <ul>
 *   <li>{@link Slf4j} — Lombok 生成 {@code log} 字段，记录拒绝与调试信息</li>
 *   <li>{@link Component} — 注册为 Spring Bean，由 Servlet 容器自动挂载</li>
 *   <li>{@link Order}（{@link Ordered#HIGHEST_PRECEDENCE}）— 尽早执行，在其他业务过滤器之前拦截非法内部请求</li>
 *   <li>{@link RequiredArgsConstructor} — Lombok 为 {@code final} 依赖生成构造器</li>
 * </ul>
 *
 * @see InternalServiceProperties#getToken()
 * @see SecurityConstants#INTERNAL_TOKEN_HEADER
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class InternalServiceAuthFilter extends OncePerRequestFilter {

    private final InternalServiceProperties internalServiceProperties;

    /**
     * 判断当前请求是否跳过本过滤器。
     * <p>
     * 仅 {@code /internal/**} 路径需要校验；其余路径直接放行。
     *
     * @param request 当前 HTTP 请求
     * @return {@code true} 表示不执行 {@link #doFilterInternal}；{@code false} 表示需要校验
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path == null || !path.startsWith("/internal/");
    }

    /**
     * 校验内部服务 Token 并继续或拒绝请求。
     * <p>
     * {@link InternalServiceProperties#getToken()} 未配置时跳过校验（本地开发）；
     * Token 不匹配时返回 HTTP 401 JSON。
     *
     * @param request     当前 HTTP 请求
     * @param response    当前 HTTP 响应
     * @param filterChain 后续过滤器链
     * @throws ServletException Servlet 处理异常
     * @throws IOException      写入响应体时可能抛出
     */
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
