package cn.org.starpivot.gateway.filter;

import cn.org.starpivot.common.security.SecurityConstants;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 剥离客户端伪造用户身份 Header 的全局过滤器（响应式）。
 * <p>
 * 在 {@link AuthGlobalFilter} 之前运行，移除请求中由客户端自行携带的
 * {@link SecurityConstants#USER_ID_HEADER} 等 Header，防止绕过 JWT 直接访问下游。
 * 运行于 WebFlux {@link GlobalFilter} 链，使用 {@link ServerWebExchange} 变更请求头，
 * <strong>非</strong> Servlet {@code HttpServletRequest} 过滤器。
 * <ul>
 *   <li>{@link Component} — 注册为 Spring Bean，自动生效于网关全局链</li>
 * </ul>
 *
 * @see AuthGlobalFilter
 * @see SecurityConstants
 */
@Component
public class StripUserHeadersFilter implements GlobalFilter, Ordered {

    /**
     * 移除所有用户身份相关 Header 后继续过滤器链。
     *
     * @param exchange 当前请求的响应式上下文
     * @param chain    网关过滤器链
     * @return 继续处理请求的 {@link Mono}
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(SecurityConstants.USER_ID_HEADER);
                    headers.remove(SecurityConstants.USER_NAME_HEADER);
                    headers.remove(SecurityConstants.ROLES_HEADER);
                    headers.remove(SecurityConstants.PERMISSIONS_HEADER);
                })
                .build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    /**
     * 过滤器顺序：最高优先级附近，确保在 JWT 解析注入 Header 之前完成剥离。
     *
     * @return {@link Ordered#HIGHEST_PRECEDENCE} + 50
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 50;
    }
}
