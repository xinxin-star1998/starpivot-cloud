package cn.org.starpivot.api.mall.order.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 待付款订单超时关单延迟消息。
 */
@Data
public class OrderCloseMessage implements Serializable {

    private Long orderId;

    private String orderSn;
}
