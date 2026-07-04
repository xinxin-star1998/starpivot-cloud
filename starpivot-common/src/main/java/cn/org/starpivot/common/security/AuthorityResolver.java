package cn.org.starpivot.common.security;

import java.util.Collection;

/**
 * 根据当前登录用户解析 Spring Security authorities（角色 + 权限标识）。
 * <p>
 * 实现类包括 {@link MenuPermissionAuthorityResolver}（查库加载菜单 perms）与
 * {@link RolesOnlyAuthorityResolver}（仅使用 JWT 角色）。
 * </p>
 */
@FunctionalInterface
public interface AuthorityResolver {

    /**
     * 将 {@link LoginUser} 转换为 Spring Security 权限字符串集合。
     *
     * @param user 当前登录用户
     * @return 角色与权限标识集合，顺序保持稳定
     */
    Collection<String> resolveAuthorities(LoginUser user);
}
