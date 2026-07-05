package cn.org.starpivot.mall.oms.service;

import cn.org.starpivot.mall.oms.domain.vo.RefundVo;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;

import java.math.BigDecimal;

/**
 * 退款处理：退货完成时按支付方式原路退款（支付宝/微信已对接，Mock 支付仅记账）。
 */
public interface OmsRefundProcessService {

    void createRefundForReturn(OmsOrder order, OmsOrderReturnApply apply, BigDecimal refundAmount);

    /**
     * 重试失败的原路退款（沿用原 refundSn 幂等）。
     */
    RefundVo retryFailedRefund(Long refundId);
}
