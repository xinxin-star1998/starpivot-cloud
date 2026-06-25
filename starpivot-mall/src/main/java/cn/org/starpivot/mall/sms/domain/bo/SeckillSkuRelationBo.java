package cn.org.starpivot.mall.sms.domain.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 秒杀 SKU 关联请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class SeckillSkuRelationBo {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * PromotionSession ID
     */
    private Long promotionSessionId;
    /**
     * SKU ID
     */
    private Long skuId;
    /**
     * seckill Price
     */
    private BigDecimal seckillPrice;
    /**
     * seckill数量
     */
    private Integer seckillCount;
    /**
     * seckill Limit
     */
    private Integer seckillLimit;
    /**
     * seckill Sort
     */
    private Integer seckillSort;
}
