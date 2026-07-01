package cn.org.starpivot.mall.pay.service;

import cn.org.starpivot.mall.pay.domain.vo.WxNativePayVo;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 微信 Native 扫码支付。
 */
public interface WxPayService {

    boolean isAvailable();

    boolean isMockMode();

    WxNativePayVo createNativePay(Long memberId, Long orderId);

    /** Mock 模式下模拟支付成功 */
    void mockConfirmPaid(Long memberId, Long orderId);

    /**
     * 处理微信支付异步通知。
     *
     * @return 应答 JSON 体（成功/失败）
     */
    String handleNotify(HttpServletRequest request, String body);
}
