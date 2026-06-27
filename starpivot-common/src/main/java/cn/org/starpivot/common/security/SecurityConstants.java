package cn.org.starpivot.common.security;

import cn.org.starpivot.common.cache.CacheConstants;

/**
 * 鉴权相关常量
 */
public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_NAME_HEADER = "X-User-Name";
    public static final String ROLES_HEADER = "X-User-Roles";
    public static final String PERMISSIONS_HEADER = "X-User-Permissions";
    public static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";
    /** @see CacheConstants#tokenBlacklistPrefix() */
    public static final String TOKEN_BLACKLIST_PREFIX = CacheConstants.tokenBlacklistPrefix();
    /** @see CacheConstants#loginRefreshPattern() 前缀部分 */
    public static final String REFRESH_TOKEN_PREFIX = CacheConstants.LOGIN_TOKENS + ":" + CacheConstants.LOGIN_REFRESH + ":";

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_SESSION_ID = "sessionId";
    public static final String CLAIM_PERMISSIONS = "permissions";
    public static final String CLAIM_ISSUED_AT = "iat";
    public static final String CLAIM_EXPIRATION = "exp";

    // 安全相关常量
    public static final String ANONYMOUS_USER = "anonymous";
    public static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";

    // Token配置常量
    public static final long DEFAULT_ACCESS_TOKEN_TTL = 7_200_000L; // 2小时
    public static final long DEFAULT_REFRESH_TOKEN_TTL = 604_800_000L; // 7天
    public static final long REFRESH_GRACE_PERIOD = 3_600_000L; // 1小时
}
