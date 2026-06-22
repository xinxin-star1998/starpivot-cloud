package cn.org.starpivot.common.security;

import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 仅使用 JWT / 网关 Header 中的角色作为 authorities，不查库。
 * <p>
 * 适用于 system 等已通过 Feign 获取完整权限的服务，或仅需角色校验的微服务。
 * </p>
 */
public final class RolesOnlyAuthorityResolver implements AuthorityResolver {

    /** 单例实例，可直接注入或引用 */
    public static final RolesOnlyAuthorityResolver INSTANCE = new RolesOnlyAuthorityResolver();

    private RolesOnlyAuthorityResolver() {
    }

    /** {@inheritDoc} */
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
