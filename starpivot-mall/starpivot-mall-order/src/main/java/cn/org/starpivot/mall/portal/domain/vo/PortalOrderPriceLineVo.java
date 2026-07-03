package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 订单计价明细行 */
@Data
public class PortalOrderPriceLineVo {

    private Long skuId;
    private String skuTitle;
    private Integer quantity;

    /** SKU 标价单价 */
    private BigDecimal originalUnitPrice;

    /** 秒杀/会员价后的单价 */
    private BigDecimal unitPrice;

    /** 标价行合计 originalUnitPrice * quantity */
    private BigDecimal lineOriginalAmount;

    /** 本行促销优惠（满减+阶梯+会员价+秒杀） */
    private BigDecimal promotionAmount;

    /** 促销后行小计 */
    private BigDecimal lineAmount;
}
