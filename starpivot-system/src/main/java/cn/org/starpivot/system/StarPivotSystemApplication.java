package cn.org.starpivot.system;

import cn.org.starpivot.common.config.OssProperties;
import cn.org.starpivot.common.security.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "cn.org.starpivot")
@EnableDiscoveryClient
@EnableAsync
@EnableConfigurationProperties({JwtProperties.class, OssProperties.class})
@MapperScan("cn.org.starpivot.system.mapper")
public class StarPivotSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarPivotSystemApplication.class, args);
    }
}
