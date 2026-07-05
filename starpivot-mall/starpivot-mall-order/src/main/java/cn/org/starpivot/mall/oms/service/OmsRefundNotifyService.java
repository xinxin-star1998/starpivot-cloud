package cn.org.starpivot.mall.oms.service;

/**
 * 退款渠道异步通知处理。
 */
public interface OmsRefundNotifyService {

    /**
     * 处理微信退款结果通知，按商户退款单号回写 {@code oms_refund_info}。
     */
    void handleWxRefundNotify(String outRefundNo, String refundStatus, String callbackJson);
}
