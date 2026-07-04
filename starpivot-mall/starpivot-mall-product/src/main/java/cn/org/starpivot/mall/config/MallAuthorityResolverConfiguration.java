package cn.org.starpivot.mall.config;

import cn.org.starpivot.common.cache.PermissionCacheService;
import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.observability.PermissionLoadFailureRecorder;
import cn.org.starpivot.common.security.AuthorityResolver;
import cn.org.starpivot.common.security.LoginUser;
import cn.org.starpivot.common.security.RolesOnlyAuthorityResolver;
import cn.org.starpivot.mall.common.MallMemberConstants;
import cn.org.starpivot.mall.security.MallPermissionLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    private final PermissionCacheService permissionCacheService;
    private final PermissionLoadFailureRecorder permissionLoadFailureRecorder;

    @Value("${spring.application.name:mall}")
    private String applicationName;

    @Bean
    public AuthorityResolver authorityResolver() {
        return new MallAuthorityResolver(
                mallPermissionLoader, permissionCacheService, permissionLoadFailureRecorder, applicationName);
    }

    @Slf4j
    static final class MallAuthorityResolver implements AuthorityResolver {

        private final MallPermissionLoader permissionLoader;
        private final PermissionCacheService permissionCacheService;
        private final PermissionLoadFailureRecorder permissionLoadFailureRecorder;
        private final String serviceName;

        MallAuthorityResolver(
                MallPermissionLoader permissionLoader,
                PermissionCacheService permissionCacheService,
                PermissionLoadFailureRecorder permissionLoadFailureRecorder,
                String serviceName) {
            this.permissionLoader = permissionLoader;
            this.permissionCacheService = permissionCacheService;
            this.permissionLoadFailureRecorder = permissionLoadFailureRecorder;
            this.serviceName = serviceName;
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
                permissionLoadFailureRecorder.record(serviceName, ex);
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
