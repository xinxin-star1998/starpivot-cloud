package cn.org.starpivot.mall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 商城安全配置：跨库读取 RBAC 时的系统库 schema 名。
 */
@Data
@Component
@ConfigurationProperties(prefix = "starpivot.mall")
public class MallSecurityProperties {

    /**
     * 与 starpivot-system 的 datasource 库名一致（本地常见 {@code starpivot}，Docker 多为 {@code star_pivot}）。
     */
    private String systemDbSchema = "star_pivot";

    /**
     * 是否开放 Mock 支付接口（生产环境应设为 false）。
     */
    private boolean mockPayEnabled = false;

    /**
     * 支付成功后经 mq_message Outbox 异步通知 ware 扣库存（需 starpivot.mq.enabled=true）。
     */
    private boolean orderPaidOutboxEnabled = false;

    /**
     * 待付款订单超时关单走 RabbitMQ 延迟队列（需 starpivot.mq.enabled=true）。
     */
    private boolean orderCloseDelayMqEnabled = false;

    /**
     * 支付成功后经 mq_message Outbox 异步确认优惠券并发放积分/成长值（需 starpivot.mq.enabled=true）。
     */
    private boolean orderPaidFollowupOutboxEnabled = false;

    public String validatedSystemDbSchema() {
        if (!StringUtils.hasText(systemDbSchema) || !systemDbSchema.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalStateException("Invalid starpivot.mall.system-db-schema: " + systemDbSchema);
        }
        return systemDbSchema;
    }
}
