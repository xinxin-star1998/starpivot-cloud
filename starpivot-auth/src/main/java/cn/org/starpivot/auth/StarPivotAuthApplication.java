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

    public static void main(String[] args) {
        SpringApplication.run(StarPivotAuthApplication.class, args);
    }
}
