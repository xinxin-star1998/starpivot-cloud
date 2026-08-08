package cn.org.starpivot.common.config;

import feign.Request;
import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 重试自动配置。
 * <p>
 * 仅注册 {@link Retryer}；熔断/超时参数统一由 Nacos Resilience4j YAML 管理，避免 Java 硬编码双轨漂移。
 * 启用重试时仅对 GET/HEAD 重试，写方法一律不重试。
 * </p>
 *
 * @see FeignRetryProperties
 */
@AutoConfiguration
@EnableConfigurationProperties(FeignRetryProperties.class)
public class FeignRetryAutoConfiguration {

    /**
     * Feign 重试器配置 — 仅当 classpath 存在 {@code feign.Retryer} 时激活。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "feign.Retryer")
    @Slf4j
    static class FeignRetryerConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "feignRetryer")
        public Retryer feignRetryer(FeignRetryProperties props) {
            if (!props.isRetryEnabled()) {
                log.info("[FeignRetry] 重试已禁用 (starpivot.feign.retry-enabled=false)");
                return Retryer.NEVER_RETRY;
            }
            log.info("[FeignRetry] 启用幂等读重试: initial={}ms, max={}ms, maxAttempts={} (仅 GET/HEAD)",
                    props.getInitialInterval(), props.getMaxInterval(), props.getMaxAttempts());
            return new IdempotentReadRetryer(
                    props.getInitialInterval(),
                    props.getMaxInterval(),
                    props.getMaxAttempts());
        }
    }

    /**
     * 仅对 GET/HEAD 委托给 {@link Retryer.Default}；写方法直接抛出，避免非幂等重试。
     */
    static final class IdempotentReadRetryer implements Retryer {

        private final long period;
        private final long maxPeriod;
        private final int maxAttempts;
        private final Retryer.Default delegate;

        IdempotentReadRetryer(long period, long maxPeriod, int maxAttempts) {
            this.period = period;
            this.maxPeriod = maxPeriod;
            this.maxAttempts = maxAttempts;
            this.delegate = new Retryer.Default(period, maxPeriod, maxAttempts);
        }

        @Override
        public void continueOrPropagate(RetryableException e) {
            Request.HttpMethod method = e.method();
            if (method != null
                    && method != Request.HttpMethod.GET
                    && method != Request.HttpMethod.HEAD) {
                throw e;
            }
            delegate.continueOrPropagate(e);
        }

        @Override
        public Retryer clone() {
            return new IdempotentReadRetryer(period, maxPeriod, maxAttempts);
        }
    }
}
