package cn.org.starpivot.common.security;

import cn.org.starpivot.common.security.LoginUser;

import java.util.Collection;

/**
 * 根据当前登录用户解析 Spring Security authorities（角色 + 权限标识）。
 */
@FunctionalInterface
public interface AuthorityResolver {

    Collection<String> resolveAuthorities(LoginUser user);
}
