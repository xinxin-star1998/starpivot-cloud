package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;

/**
 * C 端会员奖励：支付成功发放积分/成长值，退货回滚。
 */
public interface PortalMemberRewardService {

    /** 支付成功后按 SPU 积分配置发放奖励（幂等） */
    void grantOnPaid(OmsOrder order);

    /** 退货完成时按比例回滚已发奖励 */
    void clawbackOnReturn(OmsOrder order, OmsOrderReturnApply apply);
}
