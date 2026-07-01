package cn.org.starpivot.gateway.filter;

import cn.org.starpivot.common.cache.CacheConstants;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.gateway.config.GatewayRateLimitProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 网关 Redis 固定窗口限流过滤器，保护登录、验证码、短信等公开接口。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitGlobalFilter implements GlobalFilter, Ordered {

    private final GatewayRateLimitProperties rateLimitProperties;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!rateLimitProperties.isEnabled()) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();
        GatewayRateLimitProperties.Rule rule = matchRule(path);
        if (rule == null) {
            return chain.filter(exchange);
        }

        String clientKey = resolveClientKey(exchange.getRequest());
        String redisKey = CacheConstants.gatewayRateLimitKey(rule.getId(), clientKey);

        return tryAcquire(redisKey, rule.getLimit(), rule.getWindowSeconds())
                .flatMap(allowed -> {
                    if (Boolean.TRUE.equals(allowed)) {
                        return chain.filter(exchange);
                    }
                    log.warn("Rate limit exceeded: rule={}, client={}, path={}", rule.getId(), clientKey, path);
                    return tooManyRequests(exchange);
                })
                .onErrorResume(ex -> {
                    log.warn("Rate limit check failed, allowing request: rule={}, path={}, error={}",
                            rule.getId(), path, ex.getMessage());
                    return chain.filter(exchange);
                });
    }

    private GatewayRateLimitProperties.Rule matchRule(String path) {
        for (GatewayRateLimitProperties.Rule rule : rateLimitProperties.getRules()) {
            if (StringUtils.hasText(rule.getPattern()) && pathMatcher.match(rule.getPattern(), path)) {
                return rule;
            }
        }
        return null;
    }

    private Mono<Boolean> tryAcquire(String key, int limit, int windowSeconds) {
        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    Mono<Boolean> expire = count != null && count == 1L
                            ? redisTemplate.expire(key, Duration.ofSeconds(windowSeconds)).thenReturn(true)
                            : Mono.just(true);
                    return expire.thenReturn(count != null && count <= limit);
                });
    }

    private String resolveClientKey(ServerHttpRequest request) {
        String forwarded = request.getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        String realIp = request.getHeaders().getFirst("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        if (request.getRemoteAddress() != null && request.getRemoteAddress().getAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(Result.error(429, "请求过于频繁，请稍后再试"));
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            String json = "{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}";
            DataBuffer buffer = exchange.getResponse().bufferFactory()
                    .wrap(json.getBytes(StandardCharsets.UTF_8));
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
    }

    /** 在鉴权过滤器之前执行限流 */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 50;
    }
}
