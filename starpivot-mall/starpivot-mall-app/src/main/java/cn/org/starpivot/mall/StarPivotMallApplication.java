package cn.org.starpivot.mall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 商城静态资源 BFF（本地磁盘 /local-storage/**）。
 */
@SpringBootApplication(scanBasePackages = "cn.org.starpivot.mall.config")
@EnableDiscoveryClient
public class StarPivotMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarPivotMallApplication.class, args);
    }
}
