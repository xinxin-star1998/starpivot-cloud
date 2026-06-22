package cn.org.starpivot.gateway.filter;

import cn.org.starpivot.common.domain.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 内部接口路径拦截全局过滤器（响应式）。
 * <p>
 * 禁止通过网关访问包含 {@code /internal/} 的路径；此类接口仅供 Feign 直连微服务，
 * 不应暴露给外部客户端。运行于 WebFlux {@link GlobalFilter} 链，<strong>非</strong> Servlet 过滤器。
 * <ul>
 *   <li>{@link Component} — 注册为 Spring Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 注入 {@link ObjectMapper} 序列化 403 响应</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class InternalPathBlockFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper;

    /**
     * 若请求路径包含 {@code /internal/} 则返回 403 JSON，否则继续转发。
     *
     * @param exchange 当前请求的响应式上下文
     * @param chain    网关过滤器链
     * @return 拦截或继续路由的 {@link Mono}
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.contains("/internal/")) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            try {
                byte[] bytes = objectMapper.writeValueAsBytes(Result.forbidden("禁止访问内部接口"));
                DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                return exchange.getResponse().writeWith(Mono.just(buffer));
            } catch (Exception e) {
                return exchange.getResponse().setComplete();
            }
        }
        return chain.filter(exchange);
    }

    /**
     * 过滤器顺序：与 {@link StripUserHeadersFilter} 同级，尽早拦截非法内部路径访问。
     *
     * @return {@link Ordered#HIGHEST_PRECEDENCE} + 50
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 50;
    }
}
