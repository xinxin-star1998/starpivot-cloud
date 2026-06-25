package cn.org.starpivot.mall.sms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 秒杀 SKU 关联视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class SeckillSkuRelationVo {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * Promotion ID
     */
    private Long promotionId;
    /**
     * PromotionSession ID
     */
    private Long promotionSessionId;
    /**
     * session名称
     */
    private String sessionName;
    /**
     * SKU ID
     */
    private Long skuId;
    /**
     * sku名称
     */
    private String skuName;
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
