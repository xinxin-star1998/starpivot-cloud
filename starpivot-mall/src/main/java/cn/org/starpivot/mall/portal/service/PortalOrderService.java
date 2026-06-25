package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderQueryBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderSubmitBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderSubmitVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderVo;

import java.util.List;

/**
 * Orderservice服务接口。
 * <p>
 * 封装订单相关业务逻辑。
 * </p>
 */

public interface PortalOrderService {

    /**
     * 提交订单。
     */
    PortalOrderSubmitVo submit(Long memberId, PortalOrderSubmitBo bo);

    /**
     * pageMyOrders。
     */
    PageResponse<PortalOrderVo> pageMyOrders(Long memberId, PortalOrderQueryBo bo);

    /**
     * 获取MyOrder。
     */
    PortalOrderVo getMyOrder(Long memberId, Long orderId);

    /**
     * cancel。
     */
    void cancel(Long memberId, Long orderId);

    /** Mock 支付：待付款 → 待发货，并写入支付流水 */
    void mockPay(Long memberId, Long orderId);

    /** 确认收货 */
    void confirmReceive(Long memberId, Long orderId);

    /** 申请退货 */
    List<Long> applyReturn(Long memberId, cn.org.starpivot.mall.portal.domain.bo.PortalOrderReturnApplyBo bo);
}
