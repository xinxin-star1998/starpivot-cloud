package cn.org.starpivot.generator;

import cn.org.starpivot.common.security.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 代码生成服务 Spring Boot 启动类。
 * <p>
 * {@link SpringBootApplication}：启用自动配置，扫描 {@code cn.org.starpivot} 包下组件；
 * {@link EnableDiscoveryClient}：向注册中心注册本服务实例；
 * {@link EnableConfigurationProperties}：绑定 {@link JwtProperties} 配置；
 * {@link MapperScan}：扫描代码生成及公共安全相关 Mapper。
 */
@SpringBootApplication(
        scanBasePackages = "cn.org.starpivot",
        exclude = UserDetailsServiceAutoConfiguration.class)
@EnableDiscoveryClient
@EnableConfigurationProperties(JwtProperties.class)
@MapperScan({"cn.org.starpivot.generator.mapper", "cn.org.starpivot.common.security.mapper"})
public class GeneratorServiceApplication {

    /**
     * 应用入口。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(GeneratorServiceApplication.class, args);
    }
}
