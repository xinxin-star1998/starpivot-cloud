package cn.org.starpivot.api.mall.order.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class OrderRewardContextDto implements Serializable {

    private Long orderId;
    private String orderSn;
    private Long memberId;
    private Integer integration;
    private Integer growth;
    private List<OrderItemLine> items;
    /** spuId -> bounds */
    private Map<Long, SpuBoundsLine> spuBounds;

    @Data
    public static class OrderItemLine implements Serializable {
        private Long spuId;
        private Long skuId;
        private Integer skuQuantity;
    }

    @Data
    public static class SpuBoundsLine implements Serializable {
        private Long spuId;
        private BigDecimal buyBounds;
        private BigDecimal growBounds;
        private Integer work;
    }
}
