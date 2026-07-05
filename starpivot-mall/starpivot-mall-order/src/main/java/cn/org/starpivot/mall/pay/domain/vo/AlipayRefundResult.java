package cn.org.starpivot.mall.pay.domain.vo;

import lombok.Data;

/**
 * 支付宝原路退款结果摘要。
 */
@Data
public class AlipayRefundResult {

    /** 支付宝交易号 */
    private String tradeNo;

    /** 本次退款是否发生资金变动：Y/N */
    private String fundChange;

    /** 退款总金额（含退收费等，支付宝返回） */
    private String refundFee;

    /** 支付宝原始响应 JSON */
    private String rawResponse;
}
