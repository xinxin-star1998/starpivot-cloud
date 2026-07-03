package cn.org.starpivot.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * SKU 满减实体。
 * <p>
 * 对应数据库表 {@code sms_sku_full_reduction}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("sms_sku_full_reduction")
public class SmsSkuFullReduction {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * full Price
     */
    private BigDecimal fullPrice;

    /**
     * reduce Price
     */
    private BigDecimal reducePrice;

    /**
     * add Other
     */
    private Integer addOther;

}
