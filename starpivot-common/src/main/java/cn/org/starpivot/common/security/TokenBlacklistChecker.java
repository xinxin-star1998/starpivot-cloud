package cn.org.starpivot.common.security;

/**
 * 可选的 Token 黑名单校验，auth 服务实现并在过滤器中启用。
 */
@FunctionalInterface
public interface TokenBlacklistChecker {

    boolean isBlacklisted(String token);
}
