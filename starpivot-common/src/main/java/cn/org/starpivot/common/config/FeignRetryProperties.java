package cn.org.starpivot.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Feign 重试配置属性。
 * <p>
 * 绑定 {@code starpivot.feign.*}。默认关闭重试；开启后也仅对 GET/HEAD 等幂等读请求重试，
 * 避免库存扣减、下单等写操作因超时被重复执行。
 * </p>
 *
 * @see FeignRetryAutoConfiguration
 */
@Data
@ConfigurationProperties(prefix = "starpivot.feign")
public class FeignRetryProperties {

    /**
     * 是否启用 Feign 内置重试器（默认 {@code false}）。
     */
    private boolean retryEnabled = false;

    /**
     * 首次重试等待时间（毫秒）。
     */
    private long initialInterval = 1000;

    /**
     * 单次重试最大等待时间（毫秒）。
     */
    private long maxInterval = 5000;

    /**
     * 最大重试次数（含首次调用）。
     */
    private int maxAttempts = 3;
}
