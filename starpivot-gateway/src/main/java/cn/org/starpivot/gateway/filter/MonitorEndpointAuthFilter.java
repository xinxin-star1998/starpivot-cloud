package cn.org.starpivot.gateway.filter;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.security.JwtProperties;
import cn.org.starpivot.common.security.JwtUtils;
import cn.org.starpivot.common.security.SecurityConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关本地监控端点鉴权过滤器。
 * <p>
 * {@link AuthGlobalFilter} 仅作用于 Gateway 路由转发；本机 {@code @RestController}
 *（如 {@code /monitor/**}）不会经过该 GlobalFilter，因此需用 {@link WebFilter} 单独保护。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorEndpointAuthFilter implements WebFilter, Ordered {

    private final JwtProperties jwtProperties;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path == null || !path.startsWith("/monitor")) {
            return chain.filter(exchange);
        }

        String token = JwtUtils.resolveToken(
                exchange.getRequest().getHeaders().getFirst(SecurityConstants.TOKEN_HEADER));
        if (token == null || token.isEmpty()) {
            return unauthorized(exchange, "未授权，请先登录");
        }

        return isBlacklisted(token).flatMap(blacklisted -> {
            if (Boolean.TRUE.equals(blacklisted)) {
                return unauthorized(exchange, "令牌已失效，请重新登录");
            }
            try {
                JwtUtils.parseToken(token, jwtProperties.getSecret());
                return chain.filter(exchange);
            } catch (JwtException e) {
                log.warn("Invalid token for monitor endpoint: {}", e.getMessage());
                return unauthorized(exchange, "令牌无效或已过期");
            } catch (Exception e) {
                log.error("Unexpected error validating monitor endpoint token", e);
                return unauthorized(exchange, "令牌验证失败");
            }
        });
    }

    private Mono<Boolean> isBlacklisted(String token) {
        try {
            String key = SecurityConstants.TOKEN_BLACKLIST_PREFIX + JwtUtils.sanitizeTokenForBlacklist(token);
            return redisTemplate.hasKey(key)
                    .onErrorResume(e -> {
                        log.error("Redis error checking token blacklist for monitor, denying (fail-closed)", e);
                        return Mono.just(true);
                    });
        } catch (Exception e) {
            log.error("Error checking token blacklist for monitor, denying (fail-closed)", e);
            return Mono.just(true);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(Result.unauthorized(message));
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            String escaped = message.replace("\\", "\\\\").replace("\"", "\\\"");
            byte[] bytes = ("{\"code\":401,\"message\":\"" + escaped + "\"}").getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 90;
    }
}
