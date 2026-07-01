package cn.org.starpivot.mall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 商城订单计价相关配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "starpivot.mall.order")
public class MallOrderProperties {

    /** 默认运费（元），会员免邮未命中时使用 */
    private BigDecimal defaultFreight = new BigDecimal("10.00");

    /** 1 积分可抵扣金额（元），默认 100 积分 = 1 元 */
    private BigDecimal integrationYuanRate = new BigDecimal("0.01");

    /** 积分最多抵扣应付商品金额的比例（0~1） */
    private BigDecimal integrationMaxRate = new BigDecimal("0.50");
}
