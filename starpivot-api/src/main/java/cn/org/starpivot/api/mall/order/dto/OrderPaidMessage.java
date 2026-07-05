package cn.org.starpivot.api.mall.order.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 订单支付成功事件载荷（Outbox → ware 扣库存）。
 */
@Data
public class OrderPaidMessage implements Serializable {

    private Long orderId;

    private String orderSn;

    private List<OrderPaidLineDto> lines;
}
