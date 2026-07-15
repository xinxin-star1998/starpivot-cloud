package cn.org.starpivot.mall.order.controller.internal;

import cn.org.starpivot.api.mall.order.dto.*;
import cn.org.starpivot.api.mall.promotion.dto.CouponTrialItemDto;
import cn.org.starpivot.api.tms.dto.InternalOrderDeliverRequest;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.order.internal.OrderInternalService;
import cn.org.starpivot.mall.order.internal.OrderMemberInternalService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Hidden
@RestController
@RequiredArgsConstructor
public class MallOrderInternalExtensionsController {

    private final OrderInternalService orderInternalService;
    private final OrderMemberInternalService orderMemberInternalService;

    @GetMapping("/internal/mall/order/{orderId}/summary")
    public Result<OrderInternalDto> getOrderSummary(@PathVariable("orderId") Long orderId) {
        OrderInternalDto dto = orderInternalService.getOrderSummary(orderId);
        if (dto == null) {
            return Result.notFound("订单不存在");
        }
        return Result.success(dto);
    }

    @GetMapping("/internal/mall/order/{orderId}/items")
    public Result<List<OrderItemInternalDto>> listOrderItems(@PathVariable("orderId") Long orderId) {
        return Result.success(orderInternalService.listOrderItems(orderId));
    }

    @GetMapping("/internal/mall/cart/{memberId}/checked-items")
    public Result<List<CouponTrialItemDto>> listCheckedCartItems(@PathVariable("memberId") Long memberId) {
        return Result.success(orderInternalService.listCheckedCartItems(memberId));
    }

    @PostMapping("/internal/mall/order/submit")
    public Result<OrderSubmitResultDto> submitOrder(@RequestBody OrderSubmitRequestDto request) {
        return Result.success(orderInternalService.submitOrder(request));
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
        orderInternalService.syncDeliver(request);
        return Result.success();
    }

    @PutMapping("/internal/mall/order/{orderId}/confirm-receive")
    public Result<Void> syncConfirmReceive(@PathVariable("orderId") Long orderId) {
        orderInternalService.syncConfirmReceive(orderId);
        return Result.success();
    }
}
