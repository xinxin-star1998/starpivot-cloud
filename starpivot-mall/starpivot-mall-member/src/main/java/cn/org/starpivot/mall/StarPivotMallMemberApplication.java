package cn.org.starpivot.mall;

import cn.org.starpivot.common.security.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 商城会员微服务启动类。
 */
@SpringBootApplication(scanBasePackages = "cn.org.starpivot")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "cn.org.starpivot.api")
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties(JwtProperties.class)
@MapperScan({
        "cn.org.starpivot.mall.ums.mapper",
        "cn.org.starpivot.mall.portal.auth.mapper",
        "cn.org.starpivot.mall.pms.mapper",
        "cn.org.starpivot.mall.sms.mapper",
        "cn.org.starpivot.mall.oms.mapper"
})
public class StarPivotMallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarPivotMallMemberApplication.class, args);
    }
}
