package cn.org.starpivot.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 微服务鉴权策略配置属性类。
 * <p>
 * 绑定 {@code starpivot.security.*}，控制
 * {@link MicroserviceAuthenticationAutoConfiguration} 选择何种 {@link cn.org.starpivot.common.security.AuthorityResolver}。
 * <ul>
 *   <li>{@link Data} — Lombok 生成 getter/setter 等样板方法</li>
 *   <li>{@link ConfigurationProperties}（{@code prefix = "starpivot.security"}）—
 *       从配置文件或 Nacos 注入</li>
 * </ul>
 *
 * @see MicroserviceAuthenticationAutoConfiguration
 * @see cn.org.starpivot.common.security.AuthorityResolver
 */
@Data
@ConfigurationProperties(prefix = "starpivot.security")
public class MicroserviceSecurityProperties {

    /**
     * 权限解析策略，默认 {@link AuthorityStrategy#ROLES_ONLY}。
     * <p>
     * 对应配置项 {@code starpivot.security.authority-strategy}（kebab-case）。
     */
    private AuthorityStrategy authorityStrategy = AuthorityStrategy.ROLES_ONLY;

    /**
     * Spring Security 权限字符串解析策略枚举。
     *
     * @see cn.org.starpivot.common.security.RolesOnlyAuthorityResolver
     * @see cn.org.starpivot.common.security.MenuPermissionAuthorityResolver
     */
    public enum AuthorityStrategy {

        /** 仅将用户角色映射为 {@code ROLE_*} 权限（默认） */
        ROLES_ONLY,

        /** 从菜单/按钮权限表加载细粒度 permission 字符串 */
        MENU_PERMISSION,

        /** 由业务模块自行提供 {@link cn.org.starpivot.common.security.AuthorityResolver} Bean */
        CUSTOM
    }
}
