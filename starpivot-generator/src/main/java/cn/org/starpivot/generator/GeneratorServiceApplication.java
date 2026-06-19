package cn.org.starpivot.generator;

import cn.org.starpivot.common.security.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "cn.org.starpivot")
@EnableDiscoveryClient
@EnableConfigurationProperties(JwtProperties.class)
@MapperScan({"cn.org.starpivot.generator.mapper", "cn.org.starpivot.generator.security.mapper"})
public class GeneratorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeneratorServiceApplication.class, args);
    }
}
