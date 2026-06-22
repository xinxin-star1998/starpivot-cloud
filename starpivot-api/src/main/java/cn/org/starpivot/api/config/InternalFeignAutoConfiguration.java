package cn.org.starpivot.api.config;

import cn.org.starpivot.common.config.InternalServiceProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenFeign 可用时注册内部调用拦截器。
 */
@Configuration
@ConditionalOnClass(name = "feign.RequestInterceptor")
@EnableConfigurationProperties(InternalServiceProperties.class)
public class InternalFeignAutoConfiguration {

    @Bean
    public InternalFeignRequestInterceptor internalFeignRequestInterceptor(
            InternalServiceProperties internalServiceProperties) {
        return new InternalFeignRequestInterceptor(internalServiceProperties);
    }
}
