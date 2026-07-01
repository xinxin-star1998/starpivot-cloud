package cn.org.starpivot.mall.portal.service;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** 下单计价结果（内部使用） */
@Data
public class PortalOrderPricingResult {

    private BigDecimal originalAmount;
    private BigDecimal promotionAmount;
    private BigDecimal merchandiseAmount;
    private BigDecimal couponAmount;
    private Long couponId;
    private BigDecimal integrationAmount;
    private Integer useIntegration;
    private BigDecimal freightAmount;
    private boolean freeFreight;
    private BigDecimal payAmount;
    private List<PricedLine> lines;

    @Data
    public static class PricedLine {
        private Long skuId;
        private int quantity;
        private BigDecimal originalUnitPrice;
        private BigDecimal unitPrice;
        private BigDecimal lineOriginalAmount;
        private BigDecimal promotionAmount;
        private BigDecimal lineAmount;
    }
}
