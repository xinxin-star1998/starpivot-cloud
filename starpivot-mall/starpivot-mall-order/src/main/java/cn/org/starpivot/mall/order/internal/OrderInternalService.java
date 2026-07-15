package cn.org.starpivot.mall.order.internal;

import cn.org.starpivot.api.mall.order.dto.*;
import cn.org.starpivot.api.mall.promotion.PromotionInternalClient;
import cn.org.starpivot.api.mall.promotion.dto.CouponTrialItemDto;
import cn.org.starpivot.api.tms.dto.InternalOrderDeliverRequest;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.oms.domain.bo.OmsDeliverBo;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderItem;
import cn.org.starpivot.mall.oms.mapper.OmsOrderItemMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.oms.service.OmsOrderService;
import cn.org.starpivot.mall.oms.service.impl.OmsOrderLifecycleService;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderItemBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderSubmitBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCartItemVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCartVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderSubmitVo;
import cn.org.starpivot.mall.portal.service.PortalCartService;
import cn.org.starpivot.mall.portal.service.PortalOrderService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 订单域内部接口业务（Feign /internal 目标）。
 */
@Service
@RequiredArgsConstructor
public class OrderInternalService {

    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderItemMapper omsOrderItemMapper;
    private final PromotionInternalClient promotionInternalClient;
    private final PortalCartService portalCartService;
    private final PortalOrderService portalOrderService;
    private final OmsOrderService omsOrderService;
    private final OmsOrderLifecycleService omsOrderLifecycleService;

    @Transactional(readOnly = true)
    public OrderRewardContextDto getOrderRewardContext(Long orderId) {
        OmsOrder order = omsOrderMapper.selectById(orderId);
        if (order == null) {
            return null;
        }
        List<OmsOrderItem> items = omsOrderItemMapper.selectList(
                Wrappers.<OmsOrderItem>lambdaQuery().eq(OmsOrderItem::getOrderId, orderId));

        OrderRewardContextDto dto = new OrderRewardContextDto();
        dto.setOrderId(order.getId());
        dto.setOrderSn(order.getOrderSn());
        dto.setMemberId(order.getMemberId());
        dto.setIntegration(order.getIntegration());
        dto.setGrowth(order.getGrowth());
        dto.setItems(items.stream().map(this::toItemLine).collect(Collectors.toList()));
        dto.setSpuBounds(loadBoundsMap(items));
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean patchOrderReward(Long orderId, Integer integration, Integer growth) {
        OmsOrder existing = omsOrderMapper.selectById(orderId);
        if (existing == null) {
            return false;
        }
        OmsOrder patch = new OmsOrder();
        patch.setId(orderId);
        patch.setIntegration(integration);
        patch.setGrowth(growth);
        omsOrderMapper.updateById(patch);
        return true;
    }

    @Transactional(readOnly = true)
    public OrderInternalDto getOrderSummary(Long orderId) {
        OmsOrder order = omsOrderMapper.selectById(orderId);
        if (order == null) {
            return null;
        }
        OrderInternalDto dto = new OrderInternalDto();
        BeanUtils.copyProperties(order, dto);
        dto.setId(order.getId());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<OrderItemInternalDto> listOrderItems(Long orderId) {
        List<OmsOrderItem> items = omsOrderItemMapper.selectList(
                Wrappers.<OmsOrderItem>lambdaQuery().eq(OmsOrderItem::getOrderId, orderId));
        return items.stream().map(this::toItemDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CouponTrialItemDto> listCheckedCartItems(Long memberId) {
        PortalCartVo cart = portalCartService.listCart(memberId);
        if (cart.getItems() == null) {
            return List.of();
        }
        return cart.getItems().stream()
                .filter(i -> Boolean.TRUE.equals(i.getChecked()) && Boolean.TRUE.equals(i.getValid()))
                .map(this::toTrialItem)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderSubmitResultDto submitOrder(OrderSubmitRequestDto request) {
        PortalOrderSubmitBo bo = new PortalOrderSubmitBo();
        bo.setAddressId(request.getAddressId());
        bo.setUseCart(request.getUseCart());
        bo.setCartSkuIds(request.getCartSkuIds());
        bo.setNote(request.getNote());
        bo.setPayType(request.getPayType());
        bo.setCouponHistoryId(request.getCouponHistoryId());
        bo.setUseIntegration(request.getUseIntegration());
        bo.setOrderToken(request.getOrderToken());
        if (request.getItems() != null) {
            bo.setItems(request.getItems().stream().map(i -> {
                PortalOrderItemBo item = new PortalOrderItemBo();
                item.setSkuId(i.getSkuId());
                item.setQuantity(i.getQuantity());
                return item;
            }).collect(Collectors.toList()));
        }
        PortalOrderSubmitVo vo = portalOrderService.submit(request.getMemberId(), bo);
        OrderSubmitResultDto result = new OrderSubmitResultDto();
        result.setOrderId(vo.getOrderId());
        result.setOrderSn(vo.getOrderSn());
        result.setStatus(vo.getStatus());
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncDeliver(InternalOrderDeliverRequest request) {
        OmsDeliverBo bo = new OmsDeliverBo();
        bo.setOrderId(request.getOrderId());
        bo.setDeliveryCompany(request.getDeliveryCompany());
        bo.setDeliverySn(request.getDeliverySn());
        omsOrderService.deliver(bo);
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncConfirmReceive(Long orderId) {
        omsOrderLifecycleService.confirmReceive(orderId, "tms-auto");
    }

    private OrderRewardContextDto.OrderItemLine toItemLine(OmsOrderItem item) {
        OrderRewardContextDto.OrderItemLine line = new OrderRewardContextDto.OrderItemLine();
        line.setSpuId(item.getSpuId());
        line.setSkuId(item.getSkuId());
        line.setSkuQuantity(item.getSkuQuantity());
        return line;
    }

    private Map<Long, OrderRewardContextDto.SpuBoundsLine> loadBoundsMap(List<OmsOrderItem> items) {
        if (CollectionUtils.isEmpty(items)) {
            return Map.of();
        }
        Set<Long> spuIds = items.stream()
                .map(OmsOrderItem::getSpuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (spuIds.isEmpty()) {
            return Map.of();
        }
        Result<Map<Long, OrderRewardContextDto.SpuBoundsLine>> result =
                promotionInternalClient.batchSpuBounds(spuIds);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            return Map.of();
        }
        return result.getData();
    }

    private OrderItemInternalDto toItemDto(OmsOrderItem item) {
        OrderItemInternalDto dto = new OrderItemInternalDto();
        dto.setSkuId(item.getSkuId());
        dto.setSkuName(item.getSkuName());
        dto.setSkuQuantity(item.getSkuQuantity());
        return dto;
    }

    private CouponTrialItemDto toTrialItem(PortalCartItemVo item) {
        CouponTrialItemDto dto = new CouponTrialItemDto();
        dto.setSkuId(item.getSkuId());
        dto.setQuantity(item.getQuantity());
        return dto;
    }
}
