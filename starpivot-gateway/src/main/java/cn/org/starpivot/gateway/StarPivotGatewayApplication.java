package cn.org.starpivot.gateway;

import cn.org.starpivot.common.security.JwtProperties;
import cn.org.starpivot.gateway.config.GatewayAuthProperties;
import cn.org.starpivot.gateway.config.GatewayRateLimitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * StarPivot API 网关启动类。
 * <p>
 * 基于 Spring Cloud Gateway（WebFlux 响应式栈），作为统一入口负责路由转发、JWT 鉴权与链路追踪。
 * <ul>
 *   <li>{@link SpringBootApplication} — 启用自动配置；排除数据源、事务及 Servlet 安全相关配置，
 *       因网关无数据库依赖且使用自定义 {@link cn.org.starpivot.gateway.filter.AuthGlobalFilter} 鉴权，
 *       而非 Spring Security Servlet 过滤器链</li>
 *   <li>{@link EnableDiscoveryClient} — 注册到 Nacos 等服务发现，供路由动态解析下游实例</li>
 *   <li>{@link EnableConfigurationProperties} — 绑定 {@link JwtProperties} 与 {@link GatewayAuthProperties}</li>
 * </ul>
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        TransactionAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class
})
@EnableDiscoveryClient
@EnableConfigurationProperties({JwtProperties.class, GatewayAuthProperties.class, GatewayRateLimitProperties.class})
public class StarPivotGatewayApplication {

    /**
     * 启动网关应用。
     *
     * @param args 命令行参数，透传给 {@link SpringApplication#run}
     */
    public static void main(String[] args) {
        SpringApplication.run(StarPivotGatewayApplication.class, args);
    }
}
