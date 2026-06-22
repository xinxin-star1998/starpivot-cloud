package cn.org.starpivot.common.config;

import cn.org.starpivot.common.filter.MicroserviceAuthenticationFilter;
import cn.org.starpivot.common.security.AuthorityResolver;
import cn.org.starpivot.common.security.JwtProperties;
import cn.org.starpivot.common.security.MenuPermissionAuthorityResolver;
import cn.org.starpivot.common.security.RolesOnlyAuthorityResolver;
import cn.org.starpivot.common.security.TokenBlacklistChecker;
import cn.org.starpivot.common.security.mapper.AuthMenuMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 微服务统一鉴权自动配置：AuthorityResolver + MicroserviceAuthenticationFilter。
 * <p>
 * 网关等 Reactive 应用不会加载（{@link ConditionalOnWebApplication.Type#SERVLET}）。
 * 在 MyBatis-Plus 之后加载，确保 {@link AuthMenuMapper} 已注册。
 */
@AutoConfiguration(afterName = "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(UsernamePasswordAuthenticationFilter.class)
@EnableConfigurationProperties({JwtProperties.class, MicroserviceSecurityProperties.class})
public class MicroserviceAuthenticationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuthorityResolver.class)
    @ConditionalOnProperty(
            prefix = "starpivot.security",
            name = "authority-strategy",
            havingValue = "roles-only",
            matchIfMissing = true)
    public AuthorityResolver rolesOnlyAuthorityResolver() {
        return RolesOnlyAuthorityResolver.INSTANCE;
    }

    @Bean
    @ConditionalOnMissingBean(AuthorityResolver.class)
    @ConditionalOnProperty(
            prefix = "starpivot.security",
            name = "authority-strategy",
            havingValue = "menu-permission")
    @ConditionalOnClass(AuthMenuMapper.class)
    public AuthorityResolver menuPermissionAuthorityResolver(AuthMenuMapper authMenuMapper) {
        return new MenuPermissionAuthorityResolver(
                authMenuMapper::selectPermissionsByUserId,
                authMenuMapper::selectAllPermissionStrings);
    }
    @Bean
    @ConditionalOnMissingBean(MicroserviceAuthenticationFilter.class)
    public MicroserviceAuthenticationFilter microserviceAuthenticationFilter(
            JwtProperties jwtProperties,
            AuthorityResolver authorityResolver,
            ObjectProvider<TokenBlacklistChecker> tokenBlacklistChecker) {
        return new MicroserviceAuthenticationFilter(
                jwtProperties,
                authorityResolver,
                tokenBlacklistChecker.getIfAvailable());
    }
}
