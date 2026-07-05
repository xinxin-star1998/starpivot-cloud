package cn.org.starpivot.mall.oms.service;

import cn.org.starpivot.mall.oms.domain.vo.RefundVo;

/**
 * 退款状态同步（向支付渠道查询并回写）。
 */
public interface OmsRefundSyncService {

    RefundVo syncStatus(Long refundId);
}
