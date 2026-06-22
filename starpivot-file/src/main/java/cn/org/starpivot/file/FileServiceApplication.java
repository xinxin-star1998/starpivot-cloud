package cn.org.starpivot.file;

import cn.org.starpivot.common.config.OssProperties;
import cn.org.starpivot.common.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 文件服务启动类。
 * <p>
 * 提供通用上传、预签名 URL 等能力，依赖 {@link cn.org.starpivot.common.storage.FileStorageService} 访问 OSS。
 * <ul>
 *   <li>{@link SpringBootApplication} — 扫描 {@code cn.org.starpivot} 包以加载 common 模块 Bean
 *       （如 {@link cn.org.starpivot.common.filter.MicroserviceAuthenticationFilter}）</li>
 *   <li>{@link EnableDiscoveryClient} — 注册到服务发现，供网关路由转发</li>
 *   <li>{@link EnableConfigurationProperties} — 绑定 {@link JwtProperties} 与 {@link OssProperties}</li>
 * </ul>
 */
@SpringBootApplication(scanBasePackages = "cn.org.starpivot")
@EnableDiscoveryClient
@EnableConfigurationProperties({JwtProperties.class, OssProperties.class})
public class FileServiceApplication {

    /**
     * 启动文件微服务。
     *
     * @param args 命令行参数，透传给 {@link SpringApplication#run}
     */
    public static void main(String[] args) {
        SpringApplication.run(FileServiceApplication.class, args);
    }
}
