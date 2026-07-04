package cn.org.starpivot.api.mall.promotion;


import cn.org.starpivot.api.fallback.PromotionInternalClientFallbackFactory;
import cn.org.starpivot.api.mall.order.dto.OrderRewardContextDto;
import cn.org.starpivot.api.mall.promotion.dto.*;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;



/**

 * 营销服务内部接口（starpivot-mall-promotion）。

 */

@FeignClient(

        name = "starpivot-mall-promotion",

        contextId = "promotionInternalClient",

        path = "/api/${starpivot.api.version:v1}",

        fallbackFactory = PromotionInternalClientFallbackFactory.class)

public interface PromotionInternalClient {



    @PostMapping("/internal/mall/promotion/sku-price-rules")

    Result<SkuPriceRulesDto> loadSkuPriceRules(@RequestBody SkuPriceRulesRequest request);



    @PostMapping("/internal/mall/promotion/coupon/calculate")

    Result<CouponCalculateResultDto> calculateCoupon(@RequestBody CouponCalculateRequest request);



    @PostMapping("/internal/mall/promotion/coupon/consume")

    Result<Void> consumeCoupon(@RequestBody CouponConsumeRequest request);



    @PostMapping("/internal/mall/promotion/coupon/confirm-by-order/{orderId}")

    Result<Void> confirmCouponByOrder(@PathVariable("orderId") Long orderId);



    @PostMapping("/internal/mall/promotion/coupon/release-by-order/{orderId}")

    Result<Void> releaseCouponByOrder(@PathVariable("orderId") Long orderId);



    @PostMapping("/internal/mall/promotion/spu-bounds/batch")

    Result<Map<Long, OrderRewardContextDto.SpuBoundsLine>> batchSpuBounds(@RequestBody Collection<Long> spuIds);



    @PostMapping("/internal/mall/promotion/seckill/reserve")

    Result<Boolean> reserveSeckill(@RequestBody SeckillReserveRequest request);



    @PostMapping("/internal/mall/promotion/seckill/release")

    Result<Void> releaseSeckill(@RequestBody SeckillReleaseRequest request);



    @PostMapping("/internal/mall/promotion/seckill/release-by-order-sn")

    Result<Void> releaseSeckillByOrderSn(@RequestParam("orderSn") String orderSn);

    @GetMapping("/internal/mall/promotion/coupon/unused-count/{memberId}")
    Result<Integer> countUnusedCoupons(@PathVariable("memberId") Long memberId);

    @GetMapping("/internal/mall/promotion/subject/{subjectId}")
    Result<HomeSubjectDto> getSubject(@PathVariable("subjectId") Long subjectId);

}

