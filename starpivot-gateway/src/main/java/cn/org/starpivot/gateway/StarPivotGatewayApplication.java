package cn.org.starpivot.gateway;

import cn.org.starpivot.gateway.config.GatewayAuthProperties;
import cn.org.starpivot.common.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        TransactionAutoConfiguration.class
})
@EnableDiscoveryClient
@EnableConfigurationProperties({JwtProperties.class, GatewayAuthProperties.class})
public class StarPivotGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarPivotGatewayApplication.class, args);
    }
}
