package cn.org.starpivot.mall.oms.service;

import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;

import java.math.BigDecimal;

/**
 * 退款处理（Mock/记录型，生产可对接支付渠道原路退）。
 */
public interface OmsRefundProcessService {

    void createRefundForReturn(OmsOrder order, OmsOrderReturnApply apply, BigDecimal refundAmount);
}
