package cn.org.starpivot.system.service;

import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.system.domain.entity.SysRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限校验服务类。
 * <p>
 * 供 SpEL 表达式（如 {@code @ss.hasRole('admin')}）及控制器内部调用，
 * 基于当前登录用户判断角色与菜单权限。Bean 名称为 {@code ss}。
 * </p>
 * <ul>
 *   <li>{@link Slf4j} — 日志记录</li>
 *   <li>{@link Service} — Bean 名称 {@code ss}，供 {@code @PreAuthorize} SpEL 引用</li>
 * </ul>
 */
@Slf4j
@Service("ss")
@RequiredArgsConstructor
public class PermissionService {

    private final SysUserService sysUserService;

    /**
     * 判断当前用户是否拥有指定角色。
     *
     * @param roleKey 角色标识，如 {@code admin}
     * @return 拥有返回 {@code true}
     */
    public boolean hasRole(String roleKey) {
        Long userId = SecurityContextUtils.getUserId();
        if (userId == null) {
            return false;
        }
        List<SysRole> roles = sysUserService.getRolesByUserId(userId);
        return roles != null && roles.stream().anyMatch(role -> roleKey.equals(role.getRoleKey()));
    }

    /**
     * 判断当前用户是否拥有指定菜单权限标识。
     *
     * @param permission 权限字符串，如 {@code system:user:list}
     * @return 拥有返回 {@code true}
     */
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
