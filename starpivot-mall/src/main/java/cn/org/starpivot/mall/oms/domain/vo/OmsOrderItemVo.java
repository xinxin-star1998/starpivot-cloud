package cn.org.starpivot.mall.oms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细 VO
 */
@Data
public class OmsOrderItemVo {

    private Long id;

    private Long orderId;

    private String orderSn;

    private Long spuId;

    private String spuName;

    private String spuPic;

    private String spuBrand;

    private Long categoryId;

    private Long skuId;

    private String skuName;

    private String skuPic;

    private BigDecimal skuPrice;

    private Integer skuQuantity;

    private String skuAttrsVals;

    private BigDecimal promotionAmount;

    private BigDecimal couponAmount;

    private BigDecimal integrationAmount;

    private BigDecimal realAmount;

    private Integer giftIntegration;

    private Integer giftGrowth;
}
