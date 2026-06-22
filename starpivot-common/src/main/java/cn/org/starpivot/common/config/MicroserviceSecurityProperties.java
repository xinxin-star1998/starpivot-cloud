package cn.org.starpivot.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 微服务鉴权策略配置。
 */
@Data
@ConfigurationProperties(prefix = "starpivot.security")
public class MicroserviceSecurityProperties {

    /**
     * 权限解析策略：roles-only（默认）、menu-permission、custom（由业务模块自行提供 AuthorityResolver）。
     */
    private AuthorityStrategy authorityStrategy = AuthorityStrategy.ROLES_ONLY;

    public enum AuthorityStrategy {
        ROLES_ONLY,
        MENU_PERMISSION,
        CUSTOM
    }
}
