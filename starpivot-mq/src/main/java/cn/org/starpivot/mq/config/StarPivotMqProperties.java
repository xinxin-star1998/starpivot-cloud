package cn.org.starpivot.mq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * StarPivot MQ 配置项，前缀 {@code starpivot.mq}。
 */
@Data
@ConfigurationProperties(prefix = "starpivot.mq")
public class StarPivotMqProperties {

    /** 是否启用 MQ 自动配置 */
    private boolean enabled = false;

    /** 写入 envelope.producer，默认取 spring.application.name */
    private String producer = "";

    /** 幂等键 TTL */
    private Duration idempotentTtl = Duration.ofHours(24);

    private Listener listener = new Listener();

    @Data
    public static class Listener {
        private int prefetch = 10;
        private int retryMaxAttempts = 3;
    }
}
