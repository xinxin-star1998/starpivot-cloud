package cn.org.starpivot.system.service;

import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.system.domain.entity.SysRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("ss")
@RequiredArgsConstructor
public class PermissionService {

    private final SysUserService sysUserService;

    public boolean hasRole(String roleKey) {
        Long userId = SecurityContextUtils.getUserId();
        if (userId == null) {
            return false;
        }
        List<SysRole> roles = sysUserService.getRolesByUserId(userId);
        return roles != null && roles.stream().anyMatch(role -> roleKey.equals(role.getRoleKey()));
    }

    public boolean hasPermission(String permission) {
        Long userId = SecurityContextUtils.getUserId();
        if (userId == null) {
            return false;
        }
        return sysUserService.getMenuByUserId(userId).stream()
                .map(menu -> menu.getPerms())
                .filter(perms -> perms != null && !perms.isBlank())
                .anyMatch(permission::equals);
    }
}
