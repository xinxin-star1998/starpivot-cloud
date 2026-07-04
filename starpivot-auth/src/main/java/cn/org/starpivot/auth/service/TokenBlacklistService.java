package cn.org.starpivot.auth.service;

import cn.org.starpivot.auth.security.AuthTokenBlacklistChecker;
import cn.org.starpivot.common.security.JwtUtils;
import cn.org.starpivot.common.security.SecurityConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 令牌黑名单服务类。
 * <p>
 * 基于 Redis 维护已注销或强制失效的 JWT 访问令牌黑名单，
 * 供 {@link AuthTokenBlacklistChecker} 及 {@link AuthService} 在鉴权时使用。
 * </p>
 * <ul>
 *   <li>{@link Slf4j} — Lombok 生成 {@code log} 日志字段</li>
 *   <li>{@link Service} — 注册为 Spring 业务服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — Lombok 生成含 {@code final} 字段的构造器，注入 Redis 模板</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 将访问令牌加入黑名单，TTL 与 JWT 剩余有效期一致。
     *
     * @param token     JWT 访问令牌
     * @param ttlMillis 黑名单条目存活时间（毫秒）
     */
    public void add(String token, long ttlMillis) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Attempted to blacklist empty or null token");
            return;
        }

        try {
            String key = SecurityConstants.TOKEN_BLACKLIST_PREFIX + JwtUtils.sanitizeTokenForBlacklist(token);
            redisTemplate.opsForValue().set(
                    key,
                    String.valueOf(System.currentTimeMillis()), // 存储时间戳以便跟踪
                    ttlMillis,
                    TimeUnit.MILLISECONDS
            );
            log.debug("Token blacklisted: {}", key);
        } catch (Exception e) {
            log.error("Failed to blacklist token", e);
            throw e;
        }
    }

    /**
     * 检查访问令牌是否在黑名单中。
     *
     * @param token JWT 访问令牌
     * @return 在黑名单中返回 {@code true}；Redis 异常时为安全起见也返回 {@code true}
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {
            String key = SecurityConstants.TOKEN_BLACKLIST_PREFIX + JwtUtils.sanitizeTokenForBlacklist(token);
            Boolean exists = redisTemplate.hasKey(key);
            boolean isBlacklisted = Boolean.TRUE.equals(exists);

            if (isBlacklisted) {
                log.debug("Blacklisted token detected: {}", key);
            }

            return isBlacklisted;
        } catch (Exception e) {
            log.error("Error checking token blacklist status", e);
            // 在出现错误时，为了安全起见，假设令牌已被列入黑名单
            return true;
        }
    }

    /**
     * 从黑名单中移除指定令牌。
     *
     * @param token JWT 访问令牌
     */
    public void remove(String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        try {
            String key = SecurityConstants.TOKEN_BLACKLIST_PREFIX + JwtUtils.sanitizeTokenForBlacklist(token);
            redisTemplate.delete(key);
            log.debug("Token removed from blacklist: {}", key);
        } catch (Exception e) {
            log.error("Failed to remove token from blacklist", e);
        }
    }

    /**
     * 清理已过期的黑名单条目。
     * <p>
     * Redis 键已设置 TTL，过期后自动删除，此方法仅作占位与日志记录。
     * </p>
     */
    public void cleanupExpiredEntries() {
        // 注意：在Redis中，过期键会自动删除，无需手动清理
        log.info("Blacklist cleanup completed (auto-expiry handles cleanup)");
    }
}
