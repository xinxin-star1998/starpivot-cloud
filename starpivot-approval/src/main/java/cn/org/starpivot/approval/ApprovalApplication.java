package cn.org.starpivot.approval;

import cn.org.starpivot.common.security.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "cn.org.starpivot")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "cn.org.starpivot.api")
@EnableConfigurationProperties(JwtProperties.class)
@MapperScan({
        "cn.org.starpivot.approval.mapper",
        "cn.org.starpivot.common.security.mapper"
})
public class ApprovalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApprovalApplication.class, args);
    }
}
