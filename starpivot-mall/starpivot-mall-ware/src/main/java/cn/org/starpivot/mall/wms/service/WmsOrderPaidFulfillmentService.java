package cn.org.starpivot.mall.wms.service;

import cn.org.starpivot.api.mall.order.dto.OrderPaidMessage;

/**
 * 消费订单支付成功消息，扣减 WMS 库存并生成出库记录。
 */
public interface WmsOrderPaidFulfillmentService {

    void fulfill(OrderPaidMessage message);
}
