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
 * 微服务统一鉴权 Spring Boot 自动配置类。
 * <p>
 * 按策略注册 {@link AuthorityResolver}，并创建 {@link MicroserviceAuthenticationFilter}，
 * 在 Servlet 应用中统一解析 JWT 或网关透传 Header，构建 Spring Security 上下文。
 * <ul>
 *   <li>{@link AutoConfiguration}（{@code afterName = MybatisPlusAutoConfiguration}）—
 *       在 MyBatis-Plus 之后加载，确保 {@link AuthMenuMapper} 已注册</li>
 *   <li>{@link ConditionalOnWebApplication}（{@code type = SERVLET}）—
 *       仅 Servlet 微服务加载；Gateway 等 Reactive 应用不加载</li>
 *   <li>{@link ConditionalOnClass}（{@link UsernamePasswordAuthenticationFilter}）—
 *       类路径存在 Spring Security Web 时才生效</li>
 *   <li>{@link EnableConfigurationProperties} — 启用 {@link JwtProperties} 与
 *       {@link MicroserviceSecurityProperties} 属性绑定</li>
 * </ul>
 *
 * @see MicroserviceAuthenticationFilter
 * @see MicroserviceSecurityProperties
 */
@AutoConfiguration(afterName = "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(UsernamePasswordAuthenticationFilter.class)
@EnableConfigurationProperties({JwtProperties.class, MicroserviceSecurityProperties.class})
public class MicroserviceAuthenticationAutoConfiguration {

    /**
     * 注册「仅角色」权限解析器（默认策略）。
     * <p>
     * {@link ConditionalOnMissingBean} — 业务模块未自定义 {@link AuthorityResolver} 时才创建；
     * {@link ConditionalOnProperty} — {@code starpivot.security.authority-strategy=roles-only}
     * 或未配置时生效。
     *
     * @return {@link RolesOnlyAuthorityResolver} 单例
     */
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

    /**
     * 注册「菜单权限」权限解析器。
     * <p>
     * 需 {@link AuthMenuMapper} 从数据库加载用户/全局权限字符串。
     * {@link ConditionalOnProperty} — 仅当 {@code authority-strategy=menu-permission} 时生效；
     * {@link ConditionalOnClass} — 类路径存在 {@link AuthMenuMapper} 时才注册。
     *
     * @param authMenuMapper 菜单权限 MyBatis Mapper
     * @return 基于菜单权限的 {@link AuthorityResolver} 实例
     */
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

    /**
     * 注册微服务统一鉴权过滤器 Bean。
     * <p>
     * {@link ConditionalOnMissingBean} — 允许业务模块覆盖自定义实现。
     * {@link TokenBlacklistChecker} 为可选依赖，存在时用于拦截已注销 Token。
     *
     * @param jwtProperties           JWT 密钥等配置
     * @param authorityResolver         权限字符串解析策略
     * @param tokenBlacklistChecker     Token 黑名单检查器（可选）
     * @return 配置完成的 {@link MicroserviceAuthenticationFilter}
     */
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
