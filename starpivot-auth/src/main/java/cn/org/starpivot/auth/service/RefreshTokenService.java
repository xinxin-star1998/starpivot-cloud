package cn.org.starpivot.auth.service;

import cn.org.starpivot.auth.domain.RefreshTokenSession;
import cn.org.starpivot.auth.domain.UserSessionSnapshot;
import cn.org.starpivot.common.security.JwtProperties;
import cn.org.starpivot.common.security.SecurityConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 刷新令牌与会话管理（支持多设备并行登录）。
 * <p>
 * 新格式 Redis 键：{@code login_tokens:refresh:{userId}:{sessionId}}；
 * 兼容旧格式 {@code login_tokens:refresh:{userId}}（单会话）。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    static final String FIELD_TOKEN = "token";
    static final String FIELD_IP = "ip";
    static final String FIELD_BROWSER = "browser";
    static final String FIELD_OS = "os";
    static final String FIELD_LOGIN_LOCATION = "loginLocation";
    static final String FIELD_LOGIN_TIME = "loginTime";
    static final String FIELD_LAST_ACCESS_TIME = "lastAccessTime";

    private static final String PREFIX = SecurityConstants.REFRESH_TOKEN_PREFIX;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProperties jwtProperties;

    static String sessionKey(Long userId, String sessionId) {
        return PREFIX + userId + ":" + sessionId;
    }

    static String legacyKey(Long userId) {
        return PREFIX + userId;
    }

    public RefreshTokenSession createSession(Long userId, String sessionId,
                                             String ip, String browser, String os, String loginLocation) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        String resolvedSessionId = StringUtils.hasText(sessionId) ? sessionId : UUID.randomUUID().toString();
        String token = generateSecureToken();
        String key = sessionKey(userId, resolvedSessionId);
        String now = LocalDateTime.now().format(FORMATTER);

        Map<String, String> hash = new HashMap<>();
        hash.put(FIELD_TOKEN, token);
        hash.put(FIELD_IP, ip != null ? ip : "");
        hash.put(FIELD_BROWSER, browser != null ? browser : "");
        hash.put(FIELD_OS, os != null ? os : "");
        hash.put(FIELD_LOGIN_LOCATION, loginLocation != null ? loginLocation : "");
        hash.put(FIELD_LOGIN_TIME, now);
        hash.put(FIELD_LAST_ACCESS_TIME, now);

        Duration expiryDuration = resolveRefreshDuration();
        stringRedisTemplate.opsForHash().putAll(key, hash);
        stringRedisTemplate.expire(key, expiryDuration);
        log.debug("Refresh session created: userId={}, sessionId={}", userId, resolvedSessionId);
        return new RefreshTokenSession(token, resolvedSessionId);
    }

    public boolean validate(Long userId, String sessionId, String refreshToken) {
        if (userId == null || refreshToken == null || refreshToken.isBlank()) {
            return false;
        }
        if (StringUtils.hasText(sessionId)) {
            return validateSessionKey(sessionKey(userId, sessionId), refreshToken);
        }
        if (validateSessionKey(legacyKey(userId), refreshToken)) {
            return true;
        }
        for (UserSessionSnapshot snapshot : listSessions(userId)) {
            if (validateSessionKey(sessionKey(userId, snapshot.getSessionId()), refreshToken)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, String> getSessionInfo(Long userId, String sessionId) {
        if (userId == null || !StringUtils.hasText(sessionId)) {
            return Map.of();
        }
        Map<String, String> info = readHash(sessionKey(userId, sessionId));
        if (!info.isEmpty()) {
            return info;
        }
        if (String.valueOf(userId).equals(sessionId)) {
            return readHash(legacyKey(userId));
        }
        return Map.of();
    }

    public List<UserSessionSnapshot> listSessions(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<UserSessionSnapshot> sessions = new ArrayList<>();
        String legacy = legacyKey(userId);
        Map<String, String> legacyInfo = readHash(legacy);
        if (!legacyInfo.isEmpty() && legacyInfo.get(FIELD_TOKEN) != null) {
            sessions.add(new UserSessionSnapshot(String.valueOf(userId), legacyInfo));
        }
        String pattern = PREFIX + userId + ":*";
        for (String key : scanKeys(pattern)) {
            if (key.equals(legacy)) {
                continue;
            }
            String sessionId = key.substring((PREFIX + userId + ":").length());
            Map<String, String> info = readHash(key);
            if (!info.isEmpty() && info.get(FIELD_TOKEN) != null) {
                sessions.add(new UserSessionSnapshot(sessionId, info));
            }
        }
        return sessions;
    }

    public String findSessionIdByToken(Long userId, String refreshToken) {
        if (userId == null || refreshToken == null || refreshToken.isBlank()) {
            return null;
        }
        for (UserSessionSnapshot snapshot : listSessions(userId)) {
            String stored = snapshot.getAttributes().get(FIELD_TOKEN);
            if (refreshToken.equals(stored)) {
                return snapshot.getSessionId();
            }
        }
        return null;
    }

    public void revokeSession(Long userId, String sessionId) {
        if (userId == null || !StringUtils.hasText(sessionId)) {
            return;
        }
        try {
            if (String.valueOf(userId).equals(sessionId)) {
                stringRedisTemplate.delete(legacyKey(userId));
            }
            stringRedisTemplate.delete(sessionKey(userId, sessionId));
        } catch (Exception ex) {
            log.error("Error revoking session: userId={}, sessionId={}", userId, sessionId, ex);
        }
    }

    public void revokeAll(Long userId) {
        if (userId == null) {
            return;
        }
        for (UserSessionSnapshot snapshot : listSessions(userId)) {
            revokeSession(userId, snapshot.getSessionId());
        }
    }

    /** @deprecated 使用 {@link #revokeSession(Long, String)} 或 {@link #revokeAll(Long)} */
    @Deprecated
    public void revoke(Long userId) {
        revokeAll(userId);
    }

    /** @deprecated 使用 {@link #createSession(Long, String, String, String, String, String)} */
    @Deprecated
    public String createRefreshToken(Long userId, String ip, String browser, String os, String loginLocation) {
        return createSession(userId, null, ip, browser, os, loginLocation).getRefreshToken();
    }

    /** @deprecated 使用 {@link #createSession(Long, String, String, String, String, String)} */
    @Deprecated
    public String createRefreshToken(Long userId) {
        return createSession(userId, null, null, null, null, null).getRefreshToken();
    }

    /** @deprecated 使用 {@link #validate(Long, String, String)} */
    @Deprecated
    public boolean validate(Long userId, String refreshToken) {
        return validate(userId, null, refreshToken);
    }

    /** @deprecated 使用 {@link #listSessions(Long)} */
    @Deprecated
    public Map<String, String> getOnlineUserInfo(Long userId) {
        List<UserSessionSnapshot> sessions = listSessions(userId);
        return sessions.isEmpty() ? null : sessions.get(0).getAttributes();
    }

    public boolean exists(Long userId) {
        return userId != null && !listSessions(userId).isEmpty();
    }

    private boolean validateSessionKey(String key, String refreshToken) {
        try {
            String storedToken = (String) stringRedisTemplate.opsForHash().get(key, FIELD_TOKEN);
            if (storedToken == null) {
                storedToken = stringRedisTemplate.opsForValue().get(key);
            }
            if (storedToken == null || !refreshToken.equals(storedToken)) {
                return false;
            }
            stringRedisTemplate.opsForHash().put(key, FIELD_LAST_ACCESS_TIME, LocalDateTime.now().format(FORMATTER));
            return true;
        } catch (Exception ex) {
            log.error("Error validating refresh token for key={}", key, ex);
            return false;
        }
    }

    private Map<String, String> readHash(String key) {
        Map<String, String> info = new HashMap<>();
        try {
            Map<Object, Object> hash = stringRedisTemplate.opsForHash().entries(key);
            if (hash.isEmpty()) {
                String token = stringRedisTemplate.opsForValue().get(key);
                if (token != null) {
                    info.put(FIELD_TOKEN, token);
                }
                return info;
            }
            info.put(FIELD_TOKEN, (String) hash.get(FIELD_TOKEN));
            info.put(FIELD_IP, (String) hash.get(FIELD_IP));
            info.put(FIELD_BROWSER, (String) hash.get(FIELD_BROWSER));
            info.put(FIELD_OS, (String) hash.get(FIELD_OS));
            info.put(FIELD_LOGIN_LOCATION, (String) hash.get(FIELD_LOGIN_LOCATION));
            info.put(FIELD_LOGIN_TIME, (String) hash.get(FIELD_LOGIN_TIME));
            info.put(FIELD_LAST_ACCESS_TIME, (String) hash.get(FIELD_LAST_ACCESS_TIME));
        } catch (Exception ex) {
            log.error("Error reading session hash: key={}", key, ex);
        }
        return info;
    }

    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }
        return keys;
    }

    private Duration resolveRefreshDuration() {
        long refreshExpireMs = jwtProperties.getRefreshExpire();
        if (refreshExpireMs <= 0) {
            refreshExpireMs = SecurityConstants.DEFAULT_REFRESH_TOKEN_TTL;
        }
        return Duration.ofMillis(refreshExpireMs);
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
