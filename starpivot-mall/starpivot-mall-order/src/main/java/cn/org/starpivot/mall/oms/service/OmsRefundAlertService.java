package cn.org.starpivot.mall.oms.service;

import cn.org.starpivot.mall.oms.domain.vo.RefundAlertSummaryVo;

/**
 * 退款失败运维告警。
 */
public interface OmsRefundAlertService {

    RefundAlertSummaryVo summary();

    void acknowledge(Long refundId);

    void onStatusChanged(Integer previousStatus, Integer newStatus, Long refundId, String refundSn, String orderSn);
}
