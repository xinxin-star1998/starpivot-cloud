package cn.org.starpivot.common.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * 安全默认值检测自动配置。
 * <p>
 * 在所有服务中自动注册 {@link SafeDefaultsDetector}，
 * 于应用启动后检测不安全默认配置并以 {@code WARN} 日志输出告警。
 *
 * @see SafeDefaultsDetector
 */
@AutoConfiguration
public class SafeDefaultsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SafeDefaultsDetector.class)
    public SafeDefaultsDetector safeDefaultsDetector(Environment environment) {
        return new SafeDefaultsDetector(environment);
    }
}
