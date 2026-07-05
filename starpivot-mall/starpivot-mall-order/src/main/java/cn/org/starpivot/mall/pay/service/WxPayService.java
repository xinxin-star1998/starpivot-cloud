package cn.org.starpivot.mall.pay.service;

import cn.org.starpivot.mall.pay.domain.vo.WxJsapiPayVo;
import cn.org.starpivot.mall.pay.domain.vo.WxNativePayVo;
import cn.org.starpivot.mall.pay.domain.vo.WxRefundResult;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;

/**
 * 微信 Native 扫码支付。
 */
public interface WxPayService {

    boolean isAvailable();

    /** 是否已配置真实商户凭证（非 Mock） */
    boolean isConfigured();

    boolean isMockMode();

    WxNativePayVo createNativePay(Long memberId, Long orderId);

    WxJsapiPayVo createJsapiPay(Long memberId, Long orderId);

    /** Mock 模式下模拟支付成功 */
    void mockConfirmPaid(Long memberId, Long orderId);

    /**
     * 原路退款。
     *
     * @param orderSn 商户订单号
     * @param wxTransactionId 微信支付交易号
     * @param refundAmount 本次退款金额
     * @param orderPayAmount 订单原支付金额（微信 amount.total）
     * @param outRefundNo 商户退款单号（幂等）
     */
    WxRefundResult refund(
            String orderSn,
            String wxTransactionId,
            BigDecimal refundAmount,
            BigDecimal orderPayAmount,
            String outRefundNo);

    /** 按商户退款单号查询退款状态 */
    WxRefundResult queryRefund(String outRefundNo);

    /**
     * 处理微信支付异步通知。
     *
     * @return 应答 JSON 体（成功/失败）
     */
    String handleNotify(HttpServletRequest request, String body);

    /**
     * 处理微信退款异步通知。
     *
     * @return 应答 JSON 体（成功/失败）
     */
    String handleRefundNotify(HttpServletRequest request, String body);
}
