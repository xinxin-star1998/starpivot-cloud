package cn.org.starpivot.system.service;

import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.security.LoginUser;
import cn.org.starpivot.system.domain.entity.SysRole;
import cn.org.starpivot.system.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限标识加载服务类。
 * <p>
 * 为 Spring Security 的 {@link cn.org.starpivot.common.security.AuthorityResolver} 加载用户权限集合，
 * 超级管理员返回全部菜单权限，普通用户按角色菜单关联查询；结果经 {@link UserPermissionCacheService} 缓存。
 * </p>
 * <ul>
 *   <li>{@link Service} — 注册为 Spring 服务组件</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AuthorityLoaderService {

    private final SysMenuMapper sysMenuMapper;
    private final SysUserService sysUserService;
    private final UserPermissionCacheService userPermissionCacheService;

    /**
     * 加载用户的菜单权限字符串列表（不含角色标识）。
     *
     * @param user 当前登录用户
     * @return 权限标识列表，如 {@code system:user:list}
     */
    public List<String> loadPermissionStrings(LoginUser user) {
        if (user == null || user.getUserId() == null) {
            return List.of();
        }
        if (isSuperUser(user)) {
            return userPermissionCacheService.getPermissionStrings(
                    AppConstants.ADMIN_USER_ID,
                    sysMenuMapper::selectAllPermissionStrings);
        }
        return userPermissionCacheService.getPermissionStrings(
                user.getUserId(),
                () -> loadPermissionsFromDb(user.getUserId()));
    }

    /**
     * 加载用户完整权限集合（角色 Key + 菜单权限标识）。
     *
     * @param user 当前登录用户
     * @return 去重后的权限标识集合
     */
    public Set<String> loadAuthorities(LoginUser user) {
        Set<String> authoritySet = new LinkedHashSet<>();
        if (user.getRoles() != null) {
            user.getRoles().stream().filter(StringUtils::hasText).forEach(authoritySet::add);
        }
        loadPermissionStrings(user).stream()
                .filter(StringUtils::hasText)
                .forEach(authoritySet::add);
        return authoritySet;
    }

    private List<String> loadPermissionsFromDb(Long userId) {
        List<String> permissions = sysMenuMapper.selectPermissionsByUserId(userId);
        return permissions != null ? permissions : List.of();
    }

    private boolean isSuperUser(LoginUser user) {
        if (AppConstants.ADMIN_USER_ID.equals(user.getUserId())) {
            return true;
        }
        if (user.getRoles() != null
                && user.getRoles().stream().anyMatch(AppConstants.ADMIN_ROLE_KEY::equals)) {
            return true;
        }
        List<SysRole> roles = sysUserService.getRolesByUserId(user.getUserId());
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.stream().anyMatch(role ->
                AppConstants.ADMIN_ROLE_KEY.equals(role.getRoleKey())
                        || AppConstants.DataScope.ALL.equals(role.getDataScope()));
    }
}
