package cn.org.starpivot.mall.pay.domain.vo;

import lombok.Data;

/**
 * 微信原路退款结果摘要。
 */
@Data
public class WxRefundResult {

    /** 微信退款单号 */
    private String refundId;

    /** 商户退款单号 */
    private String outRefundNo;

    /** 微信支付交易号 */
    private String transactionId;

    /** 退款状态：SUCCESS / PROCESSING 等 */
    private String status;

    /** 微信原始响应 JSON */
    private String rawResponse;
}
