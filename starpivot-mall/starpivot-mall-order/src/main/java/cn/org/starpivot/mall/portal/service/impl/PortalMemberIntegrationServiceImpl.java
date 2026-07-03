package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.api.member.dto.MemberOrderIntegrationRequest;
import cn.org.starpivot.mall.common.MemberFeignSupport;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.portal.service.PortalMemberIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortalMemberIntegrationServiceImpl implements PortalMemberIntegrationService {

    private final MemberFeignSupport memberFeignSupport;
    private final OmsOrderMapper omsOrderMapper;

    @Override
    public void deductForOrder(OmsOrder order) {
        if (order == null || order.getMemberId() == null) {
            return;
        }
        int points = order.getUseIntegration() == null ? 0 : order.getUseIntegration();
        if (points <= 0) {
            return;
        }
        memberFeignSupport.deductIntegrationForOrder(toRequest(order));
    }

    @Override
    public void restoreForOrder(OmsOrder order) {
        if (order == null || order.getMemberId() == null) {
            return;
        }
        int points = order.getUseIntegration() == null ? 0 : order.getUseIntegration();
        if (points <= 0) {
            return;
        }
        memberFeignSupport.restoreIntegrationForOrder(toRequest(order));
        if (order.getId() != null) {
            OmsOrder patch = new OmsOrder();
            patch.setId(order.getId());
            patch.setUseIntegration(0);
            omsOrderMapper.updateById(patch);
            order.setUseIntegration(0);
        }
    }

    private MemberOrderIntegrationRequest toRequest(OmsOrder order) {
        MemberOrderIntegrationRequest request = new MemberOrderIntegrationRequest();
        request.setMemberId(order.getMemberId());
        request.setOrderId(order.getId());
        request.setOrderSn(order.getOrderSn());
        request.setUseIntegration(order.getUseIntegration());
        return request;
    }
}
