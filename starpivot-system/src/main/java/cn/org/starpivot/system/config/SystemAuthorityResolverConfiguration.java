package cn.org.starpivot.system.config;

import cn.org.starpivot.common.security.AuthorityResolver;
import cn.org.starpivot.system.service.AuthorityLoaderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * system 模块专属权限加载（菜单 + 按钮权限）。
 */
@Configuration
public class SystemAuthorityResolverConfiguration {

    @Bean
    public AuthorityResolver authorityResolver(AuthorityLoaderService authorityLoaderService) {
        return authorityLoaderService::loadAuthorities;
    }
}
