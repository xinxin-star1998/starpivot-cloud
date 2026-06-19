package cn.org.starpivot.auth.service;

import cn.org.starpivot.common.security.SecurityConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 令牌黑名单服务，用于处理注销和强制过期的令牌
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 将令牌加入黑名单
     *
     * @param token       令牌
     * @param ttlMillis   有效时间（毫秒）
     */
    public void add(String token, long ttlMillis) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Attempted to blacklist empty or null token");
            return;
        }

        try {
            String key = SecurityConstants.TOKEN_BLACKLIST_PREFIX + sanitizeToken(token);
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
     * 检查令牌是否在黑名单中
     *
     * @param token 令牌
     * @return 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {
            String key = SecurityConstants.TOKEN_BLACKLIST_PREFIX + sanitizeToken(token);
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
     * 移除令牌的黑名单状态（如果存在）
     *
     * @param token 令牌
     */
    public void remove(String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        try {
            String key = SecurityConstants.TOKEN_BLACKLIST_PREFIX + sanitizeToken(token);
            redisTemplate.delete(key);
            log.debug("Token removed from blacklist: {}", key);
        } catch (Exception e) {
            log.error("Failed to remove token from blacklist", e);
        }
    }

    /**
     * 清理已过期的黑名单条目（通常由定时任务调用）
     */
    public void cleanupExpiredEntries() {
        // 注意：在Redis中，过期键会自动删除，无需手动清理
        log.info("Blacklist cleanup completed (auto-expiry handles cleanup)");
    }

    /**
     * 对令牌进行安全处理，防止注入攻击
     *
     * @param token 原始令牌
     * @return 安全处理后的令牌
     */
    private String sanitizeToken(String token) {
        // 移除可能的恶意字符，只保留字母数字和标准JWT字符
        return token.replaceAll("[^a-zA-Z0-9._\\-=]", "");
    }
}
