package cn.org.starpivot.common.security;

import cn.org.starpivot.common.entity.AppConstants;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 从菜单表加载 perms 作为 authorities，供 generator / monitor 等直连 DB 的服务使用。
 * <p>
 * 超级管理员（{@link cn.org.starpivot.common.entity.AppConstants#ADMIN_USER_ID} 或 admin 角色）
 * 加载全部菜单权限；普通用户按用户 ID 查询关联 perms，并与 JWT 角色合并。
 * </p>
 */
public class MenuPermissionAuthorityResolver implements AuthorityResolver {

    private final Function<Long, List<String>> permissionsByUserId;
    private final Supplier<List<String>> allPermissions;

    /**
     * @param permissionsByUserId 按用户 ID 查询菜单 perms 的函数（通常委托 {@link cn.org.starpivot.common.security.mapper.AuthMenuMapper}）
     * @param allPermissions      超级管理员使用的全量 perms 供应器
     */
    public MenuPermissionAuthorityResolver(
            Function<Long, List<String>> permissionsByUserId,
            Supplier<List<String>> allPermissions) {
        this.permissionsByUserId = permissionsByUserId;
        this.allPermissions = allPermissions;
    }

    /**
     * {@inheritDoc}
     * <p>合并 {@link LoginUser#getRoles()} 与菜单 perms，去重并保持插入顺序。</p>
     */
    @Override
    public Collection<String> resolveAuthorities(LoginUser user) {
        Set<String> authoritySet = new LinkedHashSet<>();
        if (user.getRoles() != null) {
            user.getRoles().stream()
                    .filter(StringUtils::hasText)
                    .forEach(authoritySet::add);
        }
        List<String> permissions = isSuperUser(user)
                ? allPermissions.get()
                : permissionsByUserId.apply(user.getUserId());
        if (permissions != null) {
            permissions.stream()
                    .filter(StringUtils::hasText)
                    .forEach(authoritySet::add);
        }
        return authoritySet;
    }

    private boolean isSuperUser(LoginUser user) {
        if (user.getUserId() != null && AppConstants.ADMIN_USER_ID.equals(user.getUserId())) {
            return true;
        }
        return user.getRoles() != null
                && user.getRoles().stream().anyMatch(AppConstants.ADMIN_ROLE_KEY::equals);
    }
}
