package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.api.member.dto.MemberOrderReturnRewardRequest;
import cn.org.starpivot.api.member.dto.MemberOrderRewardRequest;
import cn.org.starpivot.mall.common.MemberFeignSupport;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;
import cn.org.starpivot.mall.portal.service.PortalMemberRewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortalMemberRewardServiceImpl implements PortalMemberRewardService {

    private final MemberFeignSupport memberFeignSupport;

    @Override
    public void grantOnPaid(OmsOrder order) {
        if (order == null || order.getId() == null) {
            return;
        }
        MemberOrderRewardRequest request = new MemberOrderRewardRequest();
        request.setOrderId(order.getId());
        memberFeignSupport.grantRewardOnPaid(request);
    }

    @Override
    public void clawbackOnReturn(OmsOrder order, OmsOrderReturnApply apply) {
        if (order == null || apply == null) {
            return;
        }
        MemberOrderReturnRewardRequest request = new MemberOrderReturnRewardRequest();
        request.setOrderId(order.getId());
        request.setOrderSn(order.getOrderSn());
        request.setMemberId(order.getMemberId());
        request.setSkuId(apply.getSkuId());
        request.setSkuCount(apply.getSkuCount());
        request.setOrderIntegration(order.getIntegration());
        request.setOrderGrowth(order.getGrowth());
        memberFeignSupport.clawbackRewardOnReturn(request);
    }
}
