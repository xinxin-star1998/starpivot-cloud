package cn.org.starpivot.auth;

import cn.org.starpivot.common.config.InternalServiceProperties;
import cn.org.starpivot.common.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证服务启动类。
 * <p>
 * 负责启动 starpivot-auth 微服务，提供登录、注册、令牌刷新、验证码等认证能力。
 * </p>
 * <ul>
 *   <li>{@link SpringBootApplication} — 启用 Spring Boot 自动配置，扫描 {@code cn.org.starpivot} 包，
 *       并排除数据源、事务及默认 {@code UserDetailsService} 自动配置（本服务无本地数据库）</li>
 *   <li>{@link EnableDiscoveryClient} — 注册到 Nacos 等服务发现组件</li>
 *   <li>{@link EnableFeignClients} — 启用 Feign 客户端，扫描 {@code cn.org.starpivot.api} 下的远程接口</li>
 *   <li>{@link EnableConfigurationProperties} — 绑定 {@link JwtProperties} 与 {@link InternalServiceProperties} 配置项</li>
 * </ul>
 */
@SpringBootApplication(
        scanBasePackages = "cn.org.starpivot",
        exclude = {
                DataSourceAutoConfiguration.class,
                TransactionAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        })
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "cn.org.starpivot.api")
@EnableConfigurationProperties({JwtProperties.class, InternalServiceProperties.class})
public class StarPivotAuthApplication {

    /**
     * 应用入口。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(StarPivotAuthApplication.class, args);
    }
}
