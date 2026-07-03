package cn.org.starpivot.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 秒杀 SKU 关联实体。
 * <p>
 * 对应数据库表 {@code sms_seckill_sku_relation}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("sms_seckill_sku_relation")
public class SmsSeckillSkuRelation {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
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
