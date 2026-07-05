package cn.org.starpivot.api.mall.order.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单支付成功消息中的 SKU 扣减行。
 */
@Data
public class OrderPaidLineDto implements Serializable {

    private Long skuId;

    private String skuName;

    private Integer quantity;
}
