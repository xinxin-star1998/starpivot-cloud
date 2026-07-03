package cn.org.starpivot.mall.common;

import cn.org.starpivot.api.mall.order.OrderInternalClient;
import cn.org.starpivot.api.mall.order.dto.OrderSubmitRequestDto;
import cn.org.starpivot.api.mall.order.dto.OrderSubmitResultDto;
import cn.org.starpivot.api.mall.promotion.dto.CouponTrialItemDto;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderItemBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderSubmitBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalSeckillOrderBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderSubmitVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderPortalFeignSupport {

    private static final String ACTION = "订单服务";

    private final OrderInternalClient orderInternalClient;

    public PortalOrderSubmitVo submitOrder(Long memberId, PortalOrderSubmitBo bo) {
        OrderSubmitRequestDto request = new OrderSubmitRequestDto();
        request.setMemberId(memberId);
        request.setAddressId(bo.getAddressId());
        request.setUseCart(bo.getUseCart());
        request.setCartSkuIds(bo.getCartSkuIds());
        request.setNote(bo.getNote());
        request.setPayType(bo.getPayType());
        request.setCouponHistoryId(bo.getCouponHistoryId());
        request.setUseIntegration(bo.getUseIntegration());
        request.setOrderToken(bo.getOrderToken());
        if (bo.getItems() != null) {
            request.setItems(bo.getItems().stream().map(this::toTrialItem).toList());
        }
        Result<OrderSubmitResultDto> result = orderInternalClient.submitOrder(request);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new BizException(result != null ? result.getMessage() : ACTION + "不可用");
        }
        OrderSubmitResultDto data = result.getData();
        PortalOrderSubmitVo vo = new PortalOrderSubmitVo();
        vo.setOrderId(data.getOrderId());
        vo.setOrderSn(data.getOrderSn());
        vo.setStatus(data.getStatus());
        return vo;
    }

    public PortalOrderSubmitVo submitSeckillOrder(Long memberId, PortalSeckillOrderBo seckillBo, List<PortalOrderItemBo> items) {
        PortalOrderSubmitBo bo = new PortalOrderSubmitBo();
        bo.setAddressId(seckillBo.getAddressId());
        bo.setUseCart(false);
        bo.setOrderToken(seckillBo.getOrderToken());
        bo.setPayType(seckillBo.getPayType());
        bo.setNote(seckillBo.getNote());
        bo.setItems(items);
        return submitOrder(memberId, bo);
    }

    private CouponTrialItemDto toTrialItem(PortalOrderItemBo item) {
        CouponTrialItemDto dto = new CouponTrialItemDto();
        dto.setSkuId(item.getSkuId());
        dto.setQuantity(item.getQuantity());
        return dto;
    }
}
