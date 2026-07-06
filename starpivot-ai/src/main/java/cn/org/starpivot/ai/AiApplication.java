package cn.org.starpivot.ai;

import cn.org.starpivot.ai.config.AiProperties;
import cn.org.starpivot.common.security.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "cn.org.starpivot")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"cn.org.starpivot.ai", "cn.org.starpivot.api"})
@MapperScan("cn.org.starpivot.ai.mapper")
@EnableConfigurationProperties({JwtProperties.class, AiProperties.class})
public class AiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
    }
}
