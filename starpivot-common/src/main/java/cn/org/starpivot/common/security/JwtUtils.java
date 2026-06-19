package cn.org.starpivot.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT 工具类，网关与鉴权服务共用
 */
public final class JwtUtils {

    private static final Map<String, SecretKey> KEY_CACHE = new ConcurrentHashMap<>();

    private JwtUtils() {
    }

    public static String createToken(LoginUser user, JwtProperties properties) {
        SecretKey key = getSigningKey(properties.getSecret());
        long expirationTime = System.currentTimeMillis() + properties.getExpire();

        return Jwts.builder()
                .subject(user.getUsername())
                .claim(SecurityConstants.CLAIM_USER_ID, user.getUserId())
                .claim(SecurityConstants.CLAIM_ROLES, user.getRoles())
                .issuedAt(new Date())
                .expiration(new Date(expirationTime))
                .signWith(key)
                .compact();
    }

    public static Claims parseToken(String token, String secret) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey(secret))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public static LoginUser toLoginUser(Claims claims) {
        Long userId = claims.get(SecurityConstants.CLAIM_USER_ID, Long.class);
        String username = claims.getSubject();
        List<String> roles = claims.get(SecurityConstants.CLAIM_ROLES, List.class);
        if (roles == null) {
            roles = Collections.emptyList();
        }
        return LoginUser.builder()
                .userId(userId)
                .username(username)
                .roles(roles)
                .build();
    }

    public static boolean isExpired(String token, String secret) {
        try {
            parseToken(token, secret);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (RuntimeException e) {
            // Token is invalid, treat as expired
            return true;
        }
    }

    public static String resolveToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return authorizationHeader.substring(SecurityConstants.TOKEN_PREFIX.length()).trim();
        }
        return null;
    }

    private static SecretKey getSigningKey(String secret) {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("JWT secret cannot be null or empty");
        }

        // Ensure secret key length is sufficient for HMAC algorithm
        if (secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters for secure signing");
        }

        return KEY_CACHE.computeIfAbsent(secret, s -> Keys.hmacShaKeyFor(s.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Validates token structure and signature without checking expiration
     */
    public static boolean isValid(String token, String secret) {
        try {
            parseToken(token, secret);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the expiration date of the token
     */
    public static Date getExpirationDateFromToken(String token, String secret) {
        Claims claims = parseToken(token, secret);
        return claims.getExpiration();
    }

    /**
     * Gets the username from the token
     */
    public static String getUsernameFromToken(String token, String secret) {
        Claims claims = parseToken(token, secret);
        return claims.getSubject();
    }

    /**
     * Checks if token is about to expire (within 5 minutes)
     */
    public static boolean isAboutToExpire(String token, String secret, long gracePeriodMs) {
        Date expiration = getExpirationDateFromToken(token, secret);
        long currentTime = System.currentTimeMillis();
        return expiration != null && (expiration.getTime() - currentTime) < gracePeriodMs;
    }
}
