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
 */
public class MenuPermissionAuthorityResolver implements AuthorityResolver {

    private final Function<Long, List<String>> permissionsByUserId;
    private final Supplier<List<String>> allPermissions;

    public MenuPermissionAuthorityResolver(
            Function<Long, List<String>> permissionsByUserId,
            Supplier<List<String>> allPermissions) {
        this.permissionsByUserId = permissionsByUserId;
        this.allPermissions = allPermissions;
    }

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
