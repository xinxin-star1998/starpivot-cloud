package cn.org.starpivot.mall.promotion.internal;

import cn.org.starpivot.api.mall.order.dto.OrderRewardContextDto;
import cn.org.starpivot.api.mall.promotion.dto.*;
import cn.org.starpivot.common.domain.Result;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

@Hidden
@RestController
@RequestMapping("/internal/mall/promotion")
@RequiredArgsConstructor
public class PromotionInternalController {

    private final PromotionInternalService promotionInternalService;

    @PostMapping("/sku-price-rules")
    public Result<SkuPriceRulesDto> loadSkuPriceRules(@RequestBody SkuPriceRulesRequest request) {
        return Result.success(promotionInternalService.loadSkuPriceRules(request));
    }

    @PostMapping("/coupon/calculate")
    public Result<CouponCalculateResultDto> calculateCoupon(@RequestBody CouponCalculateRequest request) {
        return Result.success(promotionInternalService.calculateCoupon(request));
    }

    @PostMapping("/coupon/consume")
    public Result<Void> consumeCoupon(@RequestBody CouponConsumeRequest request) {
        promotionInternalService.consumeCoupon(request);
        return Result.success();
    }

    @PostMapping("/coupon/confirm-by-order/{orderId}")
    public Result<Void> confirmCouponByOrder(@PathVariable("orderId") Long orderId) {
        promotionInternalService.confirmCouponByOrder(orderId);
        return Result.success();
    }

    @PostMapping("/coupon/release-by-order/{orderId}")
    public Result<Void> releaseCouponByOrder(@PathVariable("orderId") Long orderId) {
        promotionInternalService.releaseCouponByOrder(orderId);
        return Result.success();
    }

    @PostMapping("/spu-bounds/batch")
    public Result<Map<Long, OrderRewardContextDto.SpuBoundsLine>> batchSpuBounds(@RequestBody Collection<Long> spuIds) {
        return Result.success(promotionInternalService.batchSpuBounds(spuIds));
    }

    @PutMapping("/spu-bounds/upsert")
    public Result<Void> upsertSpuBounds(@RequestBody SpuBoundsUpsertRequest request) {
        if (request == null || request.getSpuId() == null) {
            return Result.badRequest("SPU ID不能为空");
        }
        promotionInternalService.upsertSpuBounds(request);
        return Result.success();
    }

    @PostMapping("/seckill/reserve")
    public Result<Boolean> reserveSeckill(@RequestBody SeckillReserveRequest request) {
        return Result.success(promotionInternalService.reserveSeckill(request));
    }

    @PostMapping("/seckill/release")
    public Result<Void> releaseSeckill(@RequestBody SeckillReleaseRequest request) {
        promotionInternalService.releaseSeckill(request);
        return Result.success();
    }

    @PostMapping("/seckill/release-by-order-sn")
    public Result<Void> releaseSeckillByOrderSn(@RequestParam("orderSn") String orderSn) {
        promotionInternalService.releaseSeckillByOrderSn(orderSn);
        return Result.success();
    }

    @GetMapping("/coupon/unused-count/{memberId}")
    public Result<Integer> countUnusedCoupons(@PathVariable("memberId") Long memberId) {
        return Result.success(promotionInternalService.countUnusedCoupons(memberId));
    }

    @GetMapping("/subject/{subjectId}")
    public Result<HomeSubjectDto> getSubject(@PathVariable("subjectId") Long subjectId) {
        HomeSubjectDto dto = promotionInternalService.getSubject(subjectId);
        if (dto == null) {
            return Result.notFound("专题不存在");
        }
        return Result.success(dto);
    }
}
