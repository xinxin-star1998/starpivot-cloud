package cn.org.starpivot.mall.config;

import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.security.AuthorityResolver;
import cn.org.starpivot.common.security.LoginUser;
import cn.org.starpivot.common.security.RolesOnlyAuthorityResolver;
import cn.org.starpivot.mall.common.MallMemberConstants;
import cn.org.starpivot.mall.security.MallPermissionCacheService;
import cn.org.starpivot.mall.security.MallPermissionLoader;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.util.StringUtils;

/**
 * 商城服务权限解析：后台用户走菜单 perms；C 端会员仅使用 JWT 角色。
 * <p>
 * 超级管理员判定与 system 模块 {@code AuthorityLoaderService} 对齐（JWT + 库内角色 + 全部数据权限）。
 * </p>
 */
@Configuration
@ConditionalOnProperty(
        prefix = "starpivot.security",
        name = "authority-strategy",
        havingValue = "menu-permission")
@RequiredArgsConstructor
public class MallAuthorityResolverConfiguration {

    private final MallPermissionLoader mallPermissionLoader;
    private final MallPermissionCacheService mallPermissionCacheService;

    @Bean
    public AuthorityResolver authorityResolver() {
        return new MallAuthorityResolver(mallPermissionLoader, mallPermissionCacheService);
    }

    @Slf4j
    static final class MallAuthorityResolver implements AuthorityResolver {

        private final MallPermissionLoader permissionLoader;
        private final MallPermissionCacheService permissionCacheService;

        MallAuthorityResolver(MallPermissionLoader permissionLoader, MallPermissionCacheService permissionCacheService) {
            this.permissionLoader = permissionLoader;
            this.permissionCacheService = permissionCacheService;
        }

        @Override
        public Collection<String> resolveAuthorities(LoginUser user) {
            if (user.getRoles() != null && user.getRoles().contains(MallMemberConstants.MEMBER_ROLE)) {
                return RolesOnlyAuthorityResolver.INSTANCE.resolveAuthorities(user);
            }

            Set<String> authoritySet = new LinkedHashSet<>();
            if (user.getRoles() != null) {
                user.getRoles().stream().filter(StringUtils::hasText).forEach(authoritySet::add);
            }

            try {
                List<String> permissions = isSuperUser(user)
                        ? permissionCacheService.getPermissionStrings(
                                AppConstants.ADMIN_USER_ID, permissionLoader::selectAllPermissionStrings)
                        : permissionCacheService.getPermissionStrings(
                                user.getUserId(),
                                () -> permissionLoader.selectPermissionsByUserId(user.getUserId()));
                if (permissions != null) {
                    permissions.stream().filter(StringUtils::hasText).forEach(authoritySet::add);
                }
            } catch (DataAccessException ex) {
                log.error(
                        "Failed to load mall menu permissions from system DB; check starpivot.mall.system-db-schema "
                                + "matches the system service database. Falling back to JWT roles only.",
                        ex);
            }
            return authoritySet;
        }

        private boolean isSuperUser(LoginUser user) {
            if (user.getUserId() != null && AppConstants.ADMIN_USER_ID.equals(user.getUserId())) {
                return true;
            }
            if (user.getRoles() != null
                    && user.getRoles().stream().anyMatch(AppConstants.ADMIN_ROLE_KEY::equals)) {
                return true;
            }
            if (user.getUserId() == null) {
                return false;
            }
            try {
                List<String> roleKeys = permissionLoader.selectRoleKeysByUserId(user.getUserId());
                if (roleKeys != null && roleKeys.stream().anyMatch(AppConstants.ADMIN_ROLE_KEY::equals)) {
                    return true;
                }
                return permissionLoader.hasAllDataScopeRole(user.getUserId());
            } catch (DataAccessException ex) {
                log.warn("Failed to resolve super-user role from system DB for userId={}", user.getUserId(), ex);
                return false;
            }
        }
    }
}
