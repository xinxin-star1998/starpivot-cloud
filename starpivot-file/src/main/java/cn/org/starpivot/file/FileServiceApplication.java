package cn.org.starpivot.file;

import cn.org.starpivot.common.config.OssProperties;
import cn.org.starpivot.common.security.JwtProperties;
import cn.org.starpivot.file.config.FileCenterProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 文件服务启动类。
 */
@SpringBootApplication(scanBasePackages = "cn.org.starpivot")
@EnableDiscoveryClient
@EnableConfigurationProperties({JwtProperties.class, OssProperties.class, FileCenterProperties.class})
@MapperScan({"cn.org.starpivot.file.mapper", "cn.org.starpivot.common.security.mapper"})
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
