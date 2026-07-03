package cn.org.starpivot.mall.pay.service;

import cn.org.starpivot.mall.oms.entity.OmsOrder;

/**
 * 订单支付确认（Mock / 支付宝回调共用，幂等）。
 */
public interface PortalOrderPayService {

    /**
     * 将待付款订单确认为已支付（待发货）。
     *
     * @param order           待付款订单
     * @param alipayTradeNo   支付宝交易号或 Mock 流水号
     * @param paymentStatus   支付状态（如 TRADE_SUCCESS）
     * @param callbackContent 回调原文（可空）
     * @return 是否本次新确认（false 表示已支付过，幂等跳过）
     */
    boolean confirmPaid(OmsOrder order, String alipayTradeNo, String paymentStatus, String callbackContent);
}
