package cn.org.starpivot.auth;

import cn.org.starpivot.common.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "cn.org.starpivot")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "cn.org.starpivot.api")
@EnableConfigurationProperties(JwtProperties.class)
public class StarPivotAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarPivotAuthApplication.class, args);
    }
}
