package cn.org.starpivot.mall.oms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class OmsOrderItemVo {

    /**
     * 主键 ID
     */
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
