package cn.org.starpivot.mall;

import cn.org.starpivot.common.security.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 商城微服务启动类。
 */
@SpringBootApplication(scanBasePackages = "cn.org.starpivot")
@EnableDiscoveryClient
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties(JwtProperties.class)
@MapperScan({
        "cn.org.starpivot.mall.pms.mapper",
        "cn.org.starpivot.mall.wms.mapper",
        "cn.org.starpivot.mall.oms.mapper",
        "cn.org.starpivot.mall.sms.mapper",
        "cn.org.starpivot.mall.ums.mapper"
})
public class StarPivotMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarPivotMallApplication.class, args);
    }
}
