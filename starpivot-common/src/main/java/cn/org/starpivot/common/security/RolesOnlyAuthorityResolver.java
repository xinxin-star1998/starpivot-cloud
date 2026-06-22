package cn.org.starpivot.common.security;

import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 仅使用 JWT / 网关 Header 中的角色作为 authorities，不查库。
 */
public final class RolesOnlyAuthorityResolver implements AuthorityResolver {

    public static final RolesOnlyAuthorityResolver INSTANCE = new RolesOnlyAuthorityResolver();

    private RolesOnlyAuthorityResolver() {
    }

    @Override
    public Collection<String> resolveAuthorities(LoginUser user) {
        Set<String> authoritySet = new LinkedHashSet<>();
        if (user.getRoles() != null) {
            user.getRoles().stream()
                    .filter(StringUtils::hasText)
                    .forEach(authoritySet::add);
        }
        return authoritySet;
    }
}
