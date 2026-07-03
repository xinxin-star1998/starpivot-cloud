package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.mall.oms.entity.OmsOrder;

/**
 * C 端会员积分：下单抵扣与取消回滚。
 */
public interface PortalMemberIntegrationService {

    /** 下单锁定积分（扣减余额并记流水） */
    void deductForOrder(OmsOrder order);

    /** 未支付订单取消/关单时回滚积分 */
    void restoreForOrder(OmsOrder order);
}
