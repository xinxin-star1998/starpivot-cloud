package cn.org.starpivot.auth.security;

import cn.org.starpivot.auth.service.TokenBlacklistService;
import cn.org.starpivot.common.security.TokenBlacklistChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 令牌黑名单检查器。
 * <p>
 * 实现公共模块 {@link TokenBlacklistChecker} 接口，将 JWT 黑名单校验委托给
 * {@link TokenBlacklistService}，供 {@link cn.org.starpivot.common.filter.MicroserviceAuthenticationFilter} 使用。
 * </p>
 * <ul>
 *   <li>{@link Component} — 注册为 Spring 组件，自动注入到安全过滤器链</li>
 *   <li>{@link RequiredArgsConstructor} — Lombok 生成含 {@code final} 字段的构造器，注入 {@link TokenBlacklistService}</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class AuthTokenBlacklistChecker implements TokenBlacklistChecker {

    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 判断访问令牌是否已被注销或强制失效。
     *
     * @param token JWT 访问令牌（不含 Bearer 前缀）
     * @return 若在黑名单中返回 {@code true}，否则返回 {@code false}
     */
    @Override
    public boolean isBlacklisted(String token) {
        return tokenBlacklistService.isBlacklisted(token);
    }
}
