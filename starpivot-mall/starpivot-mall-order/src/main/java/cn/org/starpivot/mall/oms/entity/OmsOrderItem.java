package cn.org.starpivot.mall.oms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细实体。
 * <p>
 * 对应数据库表 {@code oms_order_item}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("oms_order_item")
public class OmsOrderItem {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Order ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * spu名称
     */
    private String spuName;

    /**
     * spu Pic
     */
    private String spuPic;

    /**
     * spu Brand
     */
    private String spuBrand;

    /**
     * Category ID
     */
    private Long categoryId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * sku名称
     */
    private String skuName;

    /**
     * sku Pic
     */
    private String skuPic;

    /**
     * sku Price
     */
    private BigDecimal skuPrice;

    /**
     * sku Quantity
     */
    private Integer skuQuantity;

    /**
     * sku Attrs Vals
     */
    private String skuAttrsVals;

    /**
     * 金额
     */
    private BigDecimal promotionAmount;

    /**
     * 金额
     */
    private BigDecimal couponAmount;

    /**
     * 金额
     */
    private BigDecimal integrationAmount;

    /**
     * 金额
     */
    private BigDecimal realAmount;

    /**
     * gift Integration
     */
    private Integer giftIntegration;

    /**
     * gift Growth
     */
    private Integer giftGrowth;

}
