package cn.org.starpivot.approval.config;

import cn.org.starpivot.approval.security.ApprovalPermissionCacheService;
import cn.org.starpivot.approval.security.ApprovalPermissionLoader;
import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.security.AuthorityResolver;
import cn.org.starpivot.common.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * 审批服务权限解析：从 star_pivot 系统库跨 schema 加载菜单 perms。
 */
@Configuration
@ConditionalOnProperty(
        prefix = "starpivot.security",
        name = "authority-strategy",
        havingValue = "menu-permission")
@RequiredArgsConstructor
public class ApprovalAuthorityResolverConfiguration {

    private final ApprovalPermissionLoader approvalPermissionLoader;
    private final ApprovalPermissionCacheService approvalPermissionCacheService;

    @Bean
    public AuthorityResolver authorityResolver() {
        return new ApprovalAuthorityResolver(approvalPermissionLoader, approvalPermissionCacheService);
    }

    @Slf4j
    static final class ApprovalAuthorityResolver implements AuthorityResolver {

        private final ApprovalPermissionLoader permissionLoader;
        private final ApprovalPermissionCacheService permissionCacheService;

        ApprovalAuthorityResolver(
                ApprovalPermissionLoader permissionLoader,
                ApprovalPermissionCacheService permissionCacheService) {
            this.permissionLoader = permissionLoader;
            this.permissionCacheService = permissionCacheService;
        }

        @Override
        public Collection<String> resolveAuthorities(LoginUser user) {
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
                        "Failed to load approval menu permissions from system DB; check "
                                + "starpivot.approval.system-db-schema matches the system service database. "
                                + "Falling back to JWT roles only.",
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
