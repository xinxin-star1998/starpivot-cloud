package cn.org.starpivot.common.security;

/**
 * 可选的 Token 黑名单校验，auth 服务实现并在过滤器中启用。
 * <p>
 * 典型实现基于 Redis 记录已注销 Token，{@link cn.org.starpivot.common.filter.MicroserviceAuthenticationFilter}
 * 在校验 JWT 前调用 {@link #isBlacklisted(String)}。
 * </p>
 */
@FunctionalInterface
public interface TokenBlacklistChecker {

    /**
     * 判断 Token 是否已被拉黑（注销、强制下线等）。
     *
     * @param token 原始 JWT 字符串
     * @return 已拉黑返回 {@code true}
     */
    boolean isBlacklisted(String token);
}
