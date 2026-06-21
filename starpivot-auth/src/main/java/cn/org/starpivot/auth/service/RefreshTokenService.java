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
 * 刷新令牌服务
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
     * 创建刷新令牌（带客户端信息）
     *
     * @param userId 用户ID
     * @param ip 用户IP地址
     * @param browser 浏览器
     * @param os 操作系统
     * @param loginLocation 登录地点
     * @return 刷新令牌
     */
    public String createRefreshToken(Long userId, String ip, String browser, String os, String loginLocation) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        String token = generateSecureToken();
        Duration expiryDuration = Duration.ofMillis(jwtProperties.getRefreshExpire());
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
     * 创建刷新令牌（兼容旧版本）
     *
     * @param userId 用户ID
     * @return 刷新令牌
     */
    public String createRefreshToken(Long userId) {
        return createRefreshToken(userId, null, null, null, null);
    }

    /**
     * 验证刷新令牌
     *
     * @param userId         用户ID
     * @param refreshToken   刷新令牌
     * @return 是否有效
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
     * 获取在线用户的客户端信息
     *
     * @param userId 用户ID
     * @return 客户端信息Map，包含 ip, browser, os, loginLocation, loginTime, lastAccessTime
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
     * 撤销刷新令牌
     *
     * @param userId 用户ID
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

    /**
     * 生成安全的随机令牌
     *
     * @return 安全令牌
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32]; // 256 bits
        SECURE_RANDOM.nextBytes(randomBytes);
        // 使用URL安全的Base64编码
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * 更新刷新令牌的有效期（延长令牌生命周期）
     *
     * @param userId 用户ID
     * @return 是否更新成功
     */
    public boolean extendTokenExpiry(Long userId) {
        if (userId == null) {
            return false;
        }

        try {
            String currentToken = stringRedisTemplate.opsForValue().get(PREFIX + userId);
            if (currentToken != null) {
                Duration expiryDuration = Duration.ofMillis(jwtProperties.getRefreshExpire());
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
     * 检查令牌是否存在
     *
     * @param userId 用户ID
     * @return 令牌是否存在
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
