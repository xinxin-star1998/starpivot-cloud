package cn.org.starpivot.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT 工具类，网关与鉴权服务共用
 * 提供 JWT 令牌的创建、解析、验证等功能
 */
public final class JwtUtils {

    private static final JacksonSerializer<Map<String, ?>> JSON_SERIALIZER = new JacksonSerializer<>();
    private static final JacksonDeserializer<Map<String, ?>> JSON_DESERIALIZER = new JacksonDeserializer<>();

    private static final Map<String, SecretKey> KEY_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, JwtParser> PARSER_CACHE = new ConcurrentHashMap<>();

    private JwtUtils() {
    }

    /**
     * 清空密钥和解析器缓存，用于配置热更新场景。
     */
    public static void clearCache() {
        KEY_CACHE.clear();
        PARSER_CACHE.clear();
    }

    /**
     * 创建 JWT 令牌
     *
     * @param user       用户信息
     * @param properties JWT 配置属性
     * @return JWT 令牌字符串
     */
    public static String createToken(LoginUser user, JwtProperties properties) {
        validateUser(user);
        validateProperties(properties);

        SecretKey key = getSigningKey(properties.getSecret());
        long expirationTime = System.currentTimeMillis() + properties.getExpire();

        var builder = Jwts.builder()
                .json(JSON_SERIALIZER)
                .subject(user.getUsername())
                .claim(SecurityConstants.CLAIM_USER_ID, user.getUserId())
                .claim(SecurityConstants.CLAIM_ROLES, user.getRoles())
                .issuedAt(new Date())
                .expiration(new Date(expirationTime));
        if (user.getSessionId() != null && !user.getSessionId().isBlank()) {
            builder.claim(SecurityConstants.CLAIM_SESSION_ID, user.getSessionId());
        }
        return builder.signWith(key).compact();
    }

    /**
     * 解析 JWT 令牌，提取声明信息
     *
     * @param token  JWT 令牌
     * @param secret 签名密钥
     * @return Claims 对象
     * @throws JwtTokenException 如果令牌无效
     */
    public static Claims parseToken(String token, String secret) {
        validateTokenAndSecret(token, secret);

        try {
            return getParser(secret)
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new JwtTokenException(JwtTokenException.TokenError.EXPIRED, "Token has expired", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtTokenException(JwtTokenException.TokenError.UNSUPPORTED, "Unsupported JWT token", e);
        } catch (MalformedJwtException e) {
            throw new JwtTokenException(JwtTokenException.TokenError.MALFORMED, "Malformed JWT token", e);
        } catch (SignatureException e) {
            throw new JwtTokenException(JwtTokenException.TokenError.INVALID_SIGNATURE, "Invalid JWT signature", e);
        } catch (IllegalArgumentException e) {
            throw new JwtTokenException(JwtTokenException.TokenError.INVALID_FORMAT, "Invalid token format", e);
        }
    }

    /**
     * 将 Claims 转换为 LoginUser 对象
     *
     * @param claims JWT 声明
     * @return LoginUser 对象
     */
    public static LoginUser toLoginUser(Claims claims) {
        if (claims == null) {
            throw new IllegalArgumentException("Claims cannot be null");
        }

        Long userId = parseUserId(claims.get(SecurityConstants.CLAIM_USER_ID));
        String username = claims.getSubject();

        if (username == null || username.isEmpty()) {
            throw new JwtTokenException(JwtTokenException.TokenError.INVALID_CLAIMS, "Username claim is missing");
        }

        List<String> roles = claims.get(SecurityConstants.CLAIM_ROLES, List.class);
        if (roles == null) {
            roles = Collections.emptyList();
        }

        return LoginUser.builder()
                .userId(userId)
                .username(username)
                .roles(roles)
                .sessionId(claims.get(SecurityConstants.CLAIM_SESSION_ID, String.class))
                .build();
    }

    /**
     * 检查令牌是否已过期
     *
     * @param token  JWT 令牌
     * @param secret 签名密钥
     * @return true-已过期，false-未过期
     */
    public static boolean isExpired(String token, String secret) {
        try {
            parseToken(token, secret);
            return false;
        } catch (JwtTokenException e) {
            return JwtTokenException.TokenError.EXPIRED.equals(e.getError());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 从 Authorization 头中提取令牌
     *
     * @param authorizationHeader Authorization 头值
     * @return JWT 令牌（不含 Bearer 前缀），如果无效返回 null
     */
    public static String resolveToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return null;
        }

        if (!authorizationHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return null;
        }

        String token = authorizationHeader.substring(SecurityConstants.TOKEN_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    /**
     * 验证令牌结构和签名（不检查过期）
     *
     * @param token  JWT 令牌
     * @param secret 签名密钥
     * @return true-有效，false-无效
     */
    public static boolean isValid(String token, String secret) {
        try {
            validateTokenAndSecret(token, secret);
            Claims claims = getParser(secret)
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject() != null;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取令牌的过期时间
     *
     * @param token  JWT 令牌
     * @param secret 签名密钥
     * @return 过期时间
     */
    public static Date getExpirationDateFromToken(String token, String secret) {
        Claims claims = parseToken(token, secret);
        return claims.getExpiration();
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token  JWT 令牌
     * @param secret 签名密钥
     * @return 用户名
     */
    public static String getUsernameFromToken(String token, String secret) {
        Claims claims = parseToken(token, secret);
        return claims.getSubject();
    }

    /**
     * 检查令牌是否即将过期
     *
     * @param token          JWT 令牌
     * @param secret         签名密钥
     * @param gracePeriodMs  宽限期（毫秒）
     * @return true-即将过期，false-未过期
     */
    public static boolean isAboutToExpire(String token, String secret, long gracePeriodMs) {
        Date expiration = getExpirationDateFromToken(token, secret);
        if (expiration == null) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        return (expiration.getTime() - currentTime) < gracePeriodMs;
    }

    /**
     * 获取令牌的剩余有效期（秒）
     *
     * @param token  JWT 令牌
     * @param secret 签名密钥
     * @return 剩余秒数，无效返回 -1
     */
    public static long getRemainingSeconds(String token, String secret) {
        try {
            Date expiration = getExpirationDateFromToken(token, secret);
            if (expiration == null) {
                return -1;
            }
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remaining / 1000);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 获取（并缓存）JWT 解析器，显式绑定 Jackson 序列化器以避免 JJWT ServiceLoader 并发问题。
     */
    private static JwtParser getParser(String secret) {
        return PARSER_CACHE.computeIfAbsent(secret, s -> Jwts.parser()
                .json(JSON_DESERIALIZER)
                .verifyWith(getSigningKey(s))
                .build());
    }

    /**
     * 获取签名密钥
     */
    private static SecretKey getSigningKey(String secret) {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("JWT secret cannot be null or empty");
        }

        return KEY_CACHE.computeIfAbsent(secret, s -> {
            byte[] keyBytes = s.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 32) {
                try {
                    keyBytes = MessageDigest.getInstance("SHA-256").digest(keyBytes);
                } catch (NoSuchAlgorithmException e) {
                    throw new IllegalStateException("SHA-256 not available", e);
                }
            }
            return Keys.hmacShaKeyFor(keyBytes);
        });
    }

    /**
     * 验证用户信息
     */
    private static void validateUser(LoginUser user) {
        if (user == null) {
            throw new IllegalArgumentException("LoginUser cannot be null");
        }
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
    }

    /**
     * 验证配置属性
     */
    private static void validateProperties(JwtProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("JwtProperties cannot be null");
        }
        if (properties.getSecret() == null || properties.getSecret().isEmpty()) {
            throw new IllegalArgumentException("JWT secret cannot be null or empty");
        }
        if (properties.getExpire() <= 0) {
            throw new IllegalArgumentException("JWT expiration must be positive");
        }
    }

    /**
     * 验证令牌和密钥
     */
    private static void validateTokenAndSecret(String token, String secret) {
        if (token == null || token.isEmpty()) {
            throw new JwtTokenException(JwtTokenException.TokenError.INVALID_FORMAT, "Token cannot be null or empty");
        }
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("Secret cannot be null or empty");
        }
    }

    private static Long parseUserId(Object claim) {
        if (claim == null) {
            return null;
        }
        if (claim instanceof Long value) {
            return value;
        }
        if (claim instanceof Integer value) {
            return value.longValue();
        }
        if (claim instanceof Number value) {
            return value.longValue();
        }
        if (claim instanceof String text && !text.isBlank()) {
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    /**
     * JWT 令牌异常类
     */
    public static class JwtTokenException extends RuntimeException {

        private final TokenError error;

        public enum TokenError {
            EXPIRED("Token has expired"),
            INVALID_SIGNATURE("Invalid token signature"),
            MALFORMED("Malformed token structure"),
            UNSUPPORTED("Unsupported token type"),
            INVALID_FORMAT("Invalid token format"),
            INVALID_CLAIMS("Invalid token claims");

            private final String message;

            TokenError(String message) {
                this.message = message;
            }

            public String getMessage() {
                return message;
            }
        }

        public JwtTokenException(TokenError error, String message) {
            super(message);
            this.error = error;
        }

        public JwtTokenException(TokenError error, String message, Throwable cause) {
            super(message, cause);
            this.error = error;
        }

        public TokenError getError() {
            return error;
        }
    }

    /**
     * 净化 JWT 字符串用于 Redis 黑名单键，移除非法字符以防键注入。
     *
     * @param token 原始 JWT 令牌
     * @return 净化后的令牌字符串
     */
    public static String sanitizeTokenForBlacklist(String token) {
        if (token == null) {
            return "";
        }
        return token.replaceAll("[^a-zA-Z0-9._\\-=]", "");
    }
}