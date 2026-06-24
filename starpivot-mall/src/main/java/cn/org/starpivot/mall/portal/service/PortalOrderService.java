package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderQueryBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderSubmitBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderSubmitVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderVo;

public interface PortalOrderService {

    PortalOrderSubmitVo submit(Long memberId, PortalOrderSubmitBo bo);

    PageResponse<PortalOrderVo> pageMyOrders(Long memberId, PortalOrderQueryBo bo);

    PortalOrderVo getMyOrder(Long memberId, Long orderId);

    void cancel(Long memberId, Long orderId);

    /** Mock 支付：待付款 → 待发货，并写入支付流水 */
    void mockPay(Long memberId, Long orderId);
}
