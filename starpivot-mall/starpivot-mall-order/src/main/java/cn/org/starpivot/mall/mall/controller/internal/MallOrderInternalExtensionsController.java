package cn.org.starpivot.mall.mall.controller.internal;

import cn.org.starpivot.api.mall.order.dto.*;
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
import cn.org.starpivot.mall.order.internal.OrderMemberInternalService;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderItemBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderSubmitBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCartItemVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCartVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderSubmitVo;
import cn.org.starpivot.mall.portal.service.PortalCartService;
import cn.org.starpivot.mall.portal.service.PortalOrderService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Hidden
@RestController
@RequiredArgsConstructor
public class MallOrderInternalExtensionsController {

    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderItemMapper omsOrderItemMapper;
    private final PortalCartService portalCartService;
    private final PortalOrderService portalOrderService;
    private final OrderMemberInternalService orderMemberInternalService;
    private final OmsOrderService omsOrderService;
    private final OmsOrderLifecycleService omsOrderLifecycleService;

    @GetMapping("/internal/mall/order/{orderId}/summary")
    public Result<OrderInternalDto> getOrderSummary(@PathVariable("orderId") Long orderId) {
        OmsOrder order = omsOrderMapper.selectById(orderId);
        if (order == null) {
            return Result.notFound("订单不存在");
        }
        OrderInternalDto dto = new OrderInternalDto();
        BeanUtils.copyProperties(order, dto);
        dto.setId(order.getId());
        return Result.success(dto);
    }

    @GetMapping("/internal/mall/order/{orderId}/items")
    public Result<List<OrderItemInternalDto>> listOrderItems(@PathVariable("orderId") Long orderId) {
        List<OmsOrderItem> items = omsOrderItemMapper.selectList(
                Wrappers.<OmsOrderItem>lambdaQuery().eq(OmsOrderItem::getOrderId, orderId));
        return Result.success(items.stream().map(this::toItemDto).collect(Collectors.toList()));
    }

    @GetMapping("/internal/mall/cart/{memberId}/checked-items")
    public Result<List<CouponTrialItemDto>> listCheckedCartItems(@PathVariable("memberId") Long memberId) {
        PortalCartVo cart = portalCartService.listCart(memberId);
        if (cart.getItems() == null) {
            return Result.success(List.of());
        }
        List<CouponTrialItemDto> items = cart.getItems().stream()
                .filter(i -> Boolean.TRUE.equals(i.getChecked()) && Boolean.TRUE.equals(i.getValid()))
                .map(this::toTrialItem)
                .collect(Collectors.toList());
        return Result.success(items);
    }

    @PostMapping("/internal/mall/order/submit")
    public Result<OrderSubmitResultDto> submitOrder(@RequestBody OrderSubmitRequestDto request) {
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
        return Result.success(result);
    }

    @GetMapping("/internal/mall/order/member/{memberId}/count")
    public Result<Integer> countByMember(@PathVariable("memberId") Long memberId) {
        return Result.success(orderMemberInternalService.countByMember(memberId));
    }

    @GetMapping("/internal/mall/order/member/{memberId}/purchased-spu/{spuId}")
    public Result<Boolean> hasPurchasedSpu(
            @PathVariable("memberId") Long memberId,
            @PathVariable("spuId") Long spuId) {
        return Result.success(orderMemberInternalService.hasPurchasedSpu(memberId, spuId));
    }

    @GetMapping("/internal/mall/order/member/{memberId}/reviewable-purchase-items")
    public Result<List<PendingReviewItemDto>> listReviewablePurchaseItems(@PathVariable("memberId") Long memberId) {
        return Result.success(orderMemberInternalService.listReviewablePurchaseItems(memberId));
    }

    @PutMapping("/internal/mall/order/deliver")
    public Result<Void> syncDeliver(@Valid @RequestBody InternalOrderDeliverRequest request) {
        OmsDeliverBo bo = new OmsDeliverBo();
        bo.setOrderId(request.getOrderId());
        bo.setDeliveryCompany(request.getDeliveryCompany());
        bo.setDeliverySn(request.getDeliverySn());
        omsOrderService.deliver(bo);
        return Result.success();
    }

    @PutMapping("/internal/mall/order/{orderId}/confirm-receive")
    public Result<Void> syncConfirmReceive(@PathVariable("orderId") Long orderId) {
        omsOrderLifecycleService.confirmReceive(orderId, "tms-auto");
        return Result.success();
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
