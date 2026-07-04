package cn.org.starpivot.system;

import cn.org.starpivot.common.config.InternalServiceProperties;
import cn.org.starpivot.common.config.OssProperties;
import cn.org.starpivot.common.security.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 系统管理微服务启动类。
 * <p>
 * 提供用户、角色、菜单、部门、字典、日志等系统管理功能的独立 Spring Boot 应用入口。
 * </p>
 * <ul>
 *   <li>{@link SpringBootApplication} — 启用自动配置，扫描 {@code cn.org.starpivot} 包</li>
 *   <li>{@link EnableDiscoveryClient} — 注册到 Nacos 等服务发现中心</li>
 *   <li>{@link EnableAsync} — 启用异步任务（操作日志等）</li>
 *   <li>{@link EnableCaching} — 启用 Spring 缓存</li>
 *   <li>{@link EnableConfigurationProperties} — 绑定 JWT、OSS、内部服务配置属性</li>
 *   <li>{@link MapperScan} — 扫描 {@code cn.org.starpivot.system.mapper} 下的 MyBatis Mapper</li>
 * </ul>
 */
@SpringBootApplication(scanBasePackages = "cn.org.starpivot")
@EnableDiscoveryClient
@EnableAsync
@EnableCaching
@EnableConfigurationProperties({JwtProperties.class, OssProperties.class, InternalServiceProperties.class})
@MapperScan("cn.org.starpivot.system.mapper")
public class StarPivotSystemApplication {

    /**
     * 应用入口方法。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(StarPivotSystemApplication.class, args);
    }
}
