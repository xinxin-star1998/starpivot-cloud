package cn.org.starpivot.system.config;

import cn.org.starpivot.common.security.AuthorityResolver;
import cn.org.starpivot.system.service.AuthorityLoaderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 系统模块权限解析器配置类。
 * <p>
 * 将 {@link AuthorityLoaderService} 的权限加载逻辑注册为全局 {@link AuthorityResolver} Bean，
 * 供网关及其他模块按用户 ID 加载菜单与按钮权限标识。
 * </p>
 *
 * @see AuthorityLoaderService#loadAuthorities(Long)
 */
@Configuration
public class SystemAuthorityResolverConfiguration {

    /**
     * 注册权限解析器，委托 {@link AuthorityLoaderService} 加载用户权限集合。
     *
     * @param authorityLoaderService 系统权限加载服务
     * @return 方法引用形式的 {@link AuthorityResolver} 实现
     */
    @Bean
    public AuthorityResolver authorityResolver(AuthorityLoaderService authorityLoaderService) {
        return authorityLoaderService::loadAuthorities;
    }
}
