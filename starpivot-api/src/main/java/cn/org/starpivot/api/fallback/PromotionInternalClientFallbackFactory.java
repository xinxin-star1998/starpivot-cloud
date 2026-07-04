package cn.org.starpivot.api.fallback;

import cn.org.starpivot.api.mall.order.dto.OrderRewardContextDto;
import cn.org.starpivot.api.mall.promotion.PromotionInternalClient;
import cn.org.starpivot.api.mall.promotion.dto.*;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.Map;

public class PromotionInternalClientFallbackFactory implements FallbackFactory<PromotionInternalClient> {

    private static final String ACTION = "营销服务";

    @Override
    public PromotionInternalClient create(Throwable cause) {
        return new PromotionInternalClient() {
            @Override
            public Result<SkuPriceRulesDto> loadSkuPriceRules(SkuPriceRulesRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<CouponCalculateResultDto> calculateCoupon(CouponCalculateRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Void> consumeCoupon(CouponConsumeRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Void> confirmCouponByOrder(Long orderId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Void> releaseCouponByOrder(Long orderId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Map<Long, OrderRewardContextDto.SpuBoundsLine>> batchSpuBounds(Collection<Long> spuIds) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Void> upsertSpuBounds(SpuBoundsUpsertRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Boolean> reserveSeckill(SeckillReserveRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Void> releaseSeckill(SeckillReleaseRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Void> releaseSeckillByOrderSn(String orderSn) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Integer> countUnusedCoupons(Long memberId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<HomeSubjectDto> getSubject(Long subjectId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }
        };
    }
}
