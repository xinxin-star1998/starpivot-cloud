package cn.org.starpivot.mall.pay.service;

import cn.org.starpivot.mall.pay.domain.vo.AlipayPagePayVo;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付宝电脑网站支付。
 */
public interface AlipayPayService {

    /** 是否已配置并启用 */
    boolean isAvailable();

    /** 发起支付，返回自动提交表单 HTML */
    AlipayPagePayVo createPagePay(Long memberId, Long orderId);

    /**
     * 原路退款。
     *
     * @param orderSn 商户订单号
     * @param alipayTradeNo 支付宝交易号（可为空，优先使用）
     * @param refundAmount 退款金额
     * @param outRequestNo 退款请求号（幂等）
     * @return 支付宝退款单号等摘要
     */
    String refund(String orderSn, String alipayTradeNo, BigDecimal refundAmount, String outRequestNo);

    /**
     * 处理支付宝异步通知。
     *
     * @return true 表示应回复 success
     */
    boolean handleNotify(Map<String, String> params);

    Map<String, String> extractNotifyParams(HttpServletRequest request);
}
