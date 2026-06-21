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
 * 为 SecurityContext 加载用户权限标识，超级管理员与菜单树逻辑保持一致。
 */
@Service
@RequiredArgsConstructor
public class AuthorityLoaderService {

    private final SysMenuMapper sysMenuMapper;
    private final SysUserService sysUserService;

    public List<String> loadPermissionStrings(LoginUser user) {
        if (user == null || user.getUserId() == null) {
            return List.of();
        }
        if (isSuperUser(user)) {
            return sysMenuMapper.selectAllPermissionStrings();
        }
        List<String> permissions = sysMenuMapper.selectPermissionsByUserId(user.getUserId());
        return permissions != null ? permissions : List.of();
    }

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
