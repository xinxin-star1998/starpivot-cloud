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
 * 剥离客户端伪造的用户身份 Header，防止绕过 JWT 直接携带 X-User-* 访问下游服务。
 */
@Component
public class StripUserHeadersFilter implements GlobalFilter, Ordered {

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

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 50;
    }
}
