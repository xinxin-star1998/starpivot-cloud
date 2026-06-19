package cn.org.starpivot.auth.service;

import cn.org.starpivot.common.security.JwtProperties;
import cn.org.starpivot.common.security.SecurityConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Base64;
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

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProperties jwtProperties;

    /**
     * 创建刷新令牌
     *
     * @param userId 用户ID
     * @return 刷新令牌
     */
    public String createRefreshToken(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        // 生成更安全的随机令牌
        String token = generateSecureToken();
        Duration expiryDuration = Duration.ofMillis(jwtProperties.getRefreshExpire());

        stringRedisTemplate.opsForValue().set(
                PREFIX + userId,
                token,
                expiryDuration
        );

        log.debug("Refresh token created for user ID: {}", userId);
        return token;
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
            String storedToken = stringRedisTemplate.opsForValue().get(PREFIX + userId);

            if (storedToken == null) {
                log.debug("No refresh token found for user ID: {}", userId);
                return false;
            }

            boolean isValid = refreshToken.equals(storedToken);

            if (!isValid) {
                log.warn("Invalid refresh token for user ID: {}", userId);
            } else {
                log.debug("Valid refresh token for user ID: {}", userId);
            }

            return isValid;
        } catch (Exception e) {
            log.error("Error validating refresh token for user ID: {}", userId, e);
            return false;
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
