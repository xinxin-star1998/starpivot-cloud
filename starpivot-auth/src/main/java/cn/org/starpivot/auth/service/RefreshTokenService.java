package cn.org.starpivot.auth.service;

import cn.org.starpivot.common.security.JwtProperties;
import cn.org.starpivot.common.security.SecurityConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.security.SecureRandom;

/**
 * 刷新令牌服务类。
 * <p>
 * 在 Redis 中按用户 ID 存储刷新令牌及客户端信息（IP、浏览器、操作系统等），
 * 支持令牌校验、撤销、有效期延长及在线用户信息查询。
 * </p>
 * <ul>
 *   <li>{@link Slf4j} — Lombok 生成 {@code log} 日志字段</li>
 *   <li>{@link Service} — 注册为 Spring 业务服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — Lombok 生成含 {@code final} 字段的构造器，注入 Redis 模板与 {@link JwtProperties}</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String PREFIX = SecurityConstants.REFRESH_TOKEN_PREFIX;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProperties jwtProperties;

    // Hash 字段名常量
    private static final String FIELD_TOKEN = "token";
    private static final String FIELD_IP = "ip";
    private static final String FIELD_BROWSER = "browser";
    private static final String FIELD_OS = "os";
    private static final String FIELD_LOGIN_LOCATION = "loginLocation";
    private static final String FIELD_LOGIN_TIME = "loginTime";
    private static final String FIELD_LAST_ACCESS_TIME = "lastAccessTime";

    /**
     * 创建刷新令牌并记录客户端登录信息。
     *
     * @param userId        用户 ID
     * @param ip            客户端 IP 地址
     * @param browser       浏览器标识
     * @param os            操作系统标识
     * @param loginLocation 登录地理位置
     * @return 新生成的刷新令牌字符串
     * @throws IllegalArgumentException userId 为 null 时抛出
     */
    public String createRefreshToken(Long userId, String ip, String browser, String os, String loginLocation) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        String token = generateSecureToken();
        Duration expiryDuration = resolveRefreshDuration();
        String key = PREFIX + userId;
        String now = LocalDateTime.now().format(FORMATTER);

        Map<String, String> hash = new HashMap<>();
        hash.put(FIELD_TOKEN, token);
        hash.put(FIELD_IP, ip != null ? ip : "");
        hash.put(FIELD_BROWSER, browser != null ? browser : "");
        hash.put(FIELD_OS, os != null ? os : "");
        hash.put(FIELD_LOGIN_LOCATION, loginLocation != null ? loginLocation : "");
        hash.put(FIELD_LOGIN_TIME, now);
        hash.put(FIELD_LAST_ACCESS_TIME, now);

        stringRedisTemplate.opsForHash().putAll(key, hash);
        stringRedisTemplate.expire(key, expiryDuration);

        log.debug("Refresh token created for user ID: {}", userId);
        return token;
    }

    /**
     * 创建刷新令牌（不记录客户端信息，供令牌刷新场景使用）。
     *
     * @param userId 用户 ID
     * @return 新生成的刷新令牌字符串
     */
    public String createRefreshToken(Long userId) {
        return createRefreshToken(userId, null, null, null, null);
    }

    /**
     * 校验刷新令牌是否与 Redis 中存储的一致。
     *
     * @param userId       用户 ID
     * @param refreshToken 待校验的刷新令牌
     * @return 有效返回 {@code true}，无效或不存在返回 {@code false}
     */
    public boolean validate(Long userId, String refreshToken) {
        if (userId == null || refreshToken == null || refreshToken.isBlank()) {
            log.warn("Invalid parameters for refresh token validation: userId={}, refreshToken={}", userId, refreshToken);
            return false;
        }

        try {
            String key = PREFIX + userId;
            String storedToken = (String) stringRedisTemplate.opsForHash().get(key, FIELD_TOKEN);
            
            // 兼容旧版本（字符串存储方式）
            if (storedToken == null) {
                storedToken = stringRedisTemplate.opsForValue().get(key);
            }

            if (storedToken == null) {
                log.debug("No refresh token found for user ID: {}", userId);
                return false;
            }

            boolean isValid = refreshToken.equals(storedToken);

            if (!isValid) {
                log.warn("Invalid refresh token for user ID: {}", userId);
            } else {
                log.debug("Valid refresh token for user ID: {}", userId);
                // 更新最后访问时间
                stringRedisTemplate.opsForHash().put(key, FIELD_LAST_ACCESS_TIME, LocalDateTime.now().format(FORMATTER));
            }

            return isValid;
        } catch (Exception e) {
            log.error("Error validating refresh token for user ID: {}", userId, e);
            return false;
        }
    }

    /**
     * 获取在线用户的客户端信息。
     *
     * @param userId 用户 ID
     * @return 含 ip、browser、os、loginLocation、loginTime、lastAccessTime 等字段的 Map；不存在时返回 {@code null}
     */
    public Map<String, String> getOnlineUserInfo(Long userId) {
        if (userId == null) {
            return null;
        }

        try {
            String key = PREFIX + userId;
            Map<Object, Object> hash = stringRedisTemplate.opsForHash().entries(key);
            
            if (hash.isEmpty()) {
                // 尝试兼容旧版本
                String token = stringRedisTemplate.opsForValue().get(key);
                if (token == null) {
                    return null;
                }
                // 旧版本只有token，返回空的客户端信息
                Map<String, String> info = new HashMap<>();
                info.put(FIELD_TOKEN, token);
                info.put(FIELD_IP, "");
                info.put(FIELD_BROWSER, "");
                info.put(FIELD_OS, "");
                info.put(FIELD_LOGIN_LOCATION, "");
                info.put(FIELD_LOGIN_TIME, "");
                info.put(FIELD_LAST_ACCESS_TIME, "");
                return info;
            }

            Map<String, String> info = new HashMap<>();
            info.put(FIELD_TOKEN, (String) hash.get(FIELD_TOKEN));
            info.put(FIELD_IP, (String) hash.get(FIELD_IP));
            info.put(FIELD_BROWSER, (String) hash.get(FIELD_BROWSER));
            info.put(FIELD_OS, (String) hash.get(FIELD_OS));
            info.put(FIELD_LOGIN_LOCATION, (String) hash.get(FIELD_LOGIN_LOCATION));
            info.put(FIELD_LOGIN_TIME, (String) hash.get(FIELD_LOGIN_TIME));
            info.put(FIELD_LAST_ACCESS_TIME, (String) hash.get(FIELD_LAST_ACCESS_TIME));
            return info;
        } catch (Exception e) {
            log.error("Error getting online user info for user ID: {}", userId, e);
            return null;
        }
    }

    /**
     * 撤销指定用户的刷新令牌（登出或令牌轮换时调用）。
     *
     * @param userId 用户 ID
     */
    public void revoke(Long userId) {
        if (userId != null) {
            try {
                Boolean deleted = stringRedisTemplate.delete(PREFIX + userId);
                if (Boolean.TRUE.equals(deleted)) {
                    log.debug("Refresh token revoked for user ID: {}", userId);
                } else {
                    log.debug("No refresh token found to revoke for user ID: {}", userId);
                }
            } catch (Exception e) {
                log.error("Error revoking refresh token for user ID: {}", userId, e);
            }
        }
    }

    private Duration resolveRefreshDuration() {
        long refreshExpireMs = jwtProperties.getRefreshExpire();
        if (refreshExpireMs <= 0) {
            refreshExpireMs = SecurityConstants.DEFAULT_REFRESH_TOKEN_TTL;
        }
        return Duration.ofMillis(refreshExpireMs);
    }

    /**
     * 生成密码学安全的随机刷新令牌。
     *
     * @return URL 安全的 Base64 编码令牌字符串
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32]; // 256 bits
        SECURE_RANDOM.nextBytes(randomBytes);
        // 使用URL安全的Base64编码
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * 延长指定用户刷新令牌的 Redis 过期时间。
     *
     * @param userId 用户 ID
     * @return 延长成功返回 {@code true}，令牌不存在返回 {@code false}
     */
    public boolean extendTokenExpiry(Long userId) {
        if (userId == null) {
            return false;
        }

        try {
            String currentToken = stringRedisTemplate.opsForValue().get(PREFIX + userId);
            if (currentToken != null) {
                Duration expiryDuration = resolveRefreshDuration();
                stringRedisTemplate.expire(PREFIX + userId, expiryDuration);
                log.debug("Refresh token expiry extended for user ID: {}", userId);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error extending refresh token expiry for user ID: {}", userId, e);
            return false;
        }
    }

    /**
     * 检查指定用户是否存在有效的刷新令牌。
     *
     * @param userId 用户 ID
     * @return 存在返回 {@code true}，否则返回 {@code false}
     */
    public boolean exists(Long userId) {
        if (userId == null) {
            return false;
        }

        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(PREFIX + userId));
        } catch (Exception e) {
            log.error("Error checking refresh token existence for user ID: {}", userId, e);
            return false;
        }
    }
}
