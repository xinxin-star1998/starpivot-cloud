package cn.org.starpivot.mall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 秒杀相关配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "starpivot.mall.seckill")
public class MallSeckillProperties {

    /** 单会员每秒最多秒杀请求次数（Redis 滑动秒级计数） */
    private int maxRequestsPerSecond = 10;

    /** 秒杀订单未支付库存锁超时（分钟），覆盖默认 30 分钟 */
    private int unpaidLockMinutes = 5;
}
