package cn.org.starpivot.api.config;

import cn.org.starpivot.api.fallback.*;
import cn.org.starpivot.common.config.InternalServiceProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 内部调用配置类。
 * <p>
 * 通过各业务模块 {@code scanBasePackages = "cn.org.starpivot"} 扫描加载；
 * 在 classpath 存在 OpenFeign 时注册拦截器与各 Feign Client 的 FallbackFactory。
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

    @Bean
    public FeignAuthForwardInterceptor feignAuthForwardInterceptor() {
        return new FeignAuthForwardInterceptor();
    }

    @Bean
    public SysUserClientFallbackFactory sysUserClientFallbackFactory() {
        return new SysUserClientFallbackFactory();
    }

    @Bean
    public SysConfigClientFallbackFactory sysConfigClientFallbackFactory() {
        return new SysConfigClientFallbackFactory();
    }

    @Bean
    public SysLoginLogClientFallbackFactory sysLoginLogClientFallbackFactory() {
        return new SysLoginLogClientFallbackFactory();
    }

    @Bean
    public SysOperLogClientFallbackFactory sysOperLogClientFallbackFactory() {
        return new SysOperLogClientFallbackFactory();
    }

    @Bean
    public SysOrgClientFallbackFactory sysOrgClientFallbackFactory() {
        return new SysOrgClientFallbackFactory();
    }

    @Bean
    public SysMessageClientFallbackFactory sysMessageClientFallbackFactory() {
        return new SysMessageClientFallbackFactory();
    }

    @Bean
    public FileRefClientFallbackFactory fileRefClientFallbackFactory() {
        return new FileRefClientFallbackFactory();
    }

    @Bean
    public ApprovalInternalClientFallbackFactory approvalInternalClientFallbackFactory() {
        return new ApprovalInternalClientFallbackFactory();
    }

    @Bean
    public OrderInternalClientFallbackFactory orderInternalClientFallbackFactory() {
        return new OrderInternalClientFallbackFactory();
    }

    @Bean
    public CartInternalClientFallbackFactory cartInternalClientFallbackFactory() {
        return new CartInternalClientFallbackFactory();
    }

    @Bean
    public MallOrderInternalClientFallbackFactory mallOrderInternalClientFallbackFactory() {
        return new MallOrderInternalClientFallbackFactory();
    }

    @Bean
    public MallStockInternalClientFallbackFactory mallStockInternalClientFallbackFactory() {
        return new MallStockInternalClientFallbackFactory();
    }

    @Bean
    public WareInternalClientFallbackFactory wareInternalClientFallbackFactory() {
        return new WareInternalClientFallbackFactory();
    }

    @Bean
    public ProductInternalClientFallbackFactory productInternalClientFallbackFactory() {
        return new ProductInternalClientFallbackFactory();
    }

    @Bean
    public MemberInternalClientFallbackFactory memberInternalClientFallbackFactory() {
        return new MemberInternalClientFallbackFactory();
    }

    @Bean
    public PromotionInternalClientFallbackFactory promotionInternalClientFallbackFactory() {
        return new PromotionInternalClientFallbackFactory();
    }

    @Bean
    public TmsInternalClientFallbackFactory tmsInternalClientFallbackFactory() {
        return new TmsInternalClientFallbackFactory();
    }
}
