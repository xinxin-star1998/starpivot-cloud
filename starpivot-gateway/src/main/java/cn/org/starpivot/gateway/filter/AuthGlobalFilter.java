package cn.org.starpivot.gateway.filter;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.gateway.config.GatewayAuthProperties;
import cn.org.starpivot.common.security.JwtProperties;
import cn.org.starpivot.common.security.JwtUtils;
import cn.org.starpivot.common.security.LoginUser;
import cn.org.starpivot.common.security.SecurityConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
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
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关认证过滤器，负责验证JWT令牌并传递用户信息给下游服务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtProperties jwtProperties;
    private final GatewayAuthProperties gatewayAuthProperties;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 检查是否为白名单路径
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        // 从请求头获取令牌
        String token = JwtUtils.resolveToken(
                exchange.getRequest().getHeaders().getFirst(SecurityConstants.TOKEN_HEADER));

        if (token == null || token.isEmpty()) {
            return unauthorized(exchange, "未授权，请先登录");
        }

        // 检查令牌是否在黑名单中
        return isBlacklisted(token)
                .flatMap(blacklisted -> {
                    if (Boolean.TRUE.equals(blacklisted)) {
                        return unauthorized(exchange, "令牌已失效，请重新登录");
                    }

                    try {
                        // 解析JWT令牌并获取用户信息
                        LoginUser user = JwtUtils.toLoginUser(
                                JwtUtils.parseToken(token, jwtProperties.getSecret()));

                        // 如果用户信息有效，则构建带有用户信息的请求
                        ServerHttpRequest mutated = exchange.getRequest().mutate()
                                .header(SecurityConstants.USER_ID_HEADER, String.valueOf(user.getUserId()))
                                .header(SecurityConstants.USER_NAME_HEADER, user.getUsername())
                                .header(SecurityConstants.ROLES_HEADER, String.join(",", user.getRoles()))
                                .build();

                        return chain.filter(exchange.mutate().request(mutated).build());
                    } catch (JwtException e) {
                        log.warn("Invalid token: {}", e.getMessage());
                        return unauthorized(exchange, "令牌无效或已过期");
                    } catch (Exception e) {
                        log.error("Unexpected error during token validation", e);
                        return unauthorized(exchange, "令牌验证失败");
                    }
                });
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhitelisted(String path) {
        return gatewayAuthProperties.getWhitelist().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 检查令牌是否在黑名单中
     */
    private Mono<Boolean> isBlacklisted(String token) {
        try {
            return redisTemplate.hasKey(SecurityConstants.TOKEN_BLACKLIST_PREFIX + token);
        } catch (Exception e) {
            log.error("Error checking token blacklist status", e);
            // 发生错误时，默认认为不在黑名单中，以避免阻止合法请求
            return Mono.just(false);
        }
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(Result.unauthorized(message));
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error serializing unauthorized response", e);
            String jsonResponse = "{\"code\":" + HttpStatus.UNAUTHORIZED.value() + ",\"message\":\"" + message + "\"}";
            byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}
