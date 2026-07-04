package cn.org.starpivot.api.fallback;

import cn.org.starpivot.api.mall.order.OrderInternalClient;
import cn.org.starpivot.api.mall.order.dto.*;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;

public class OrderInternalClientFallbackFactory implements FallbackFactory<OrderInternalClient> {

    private static final String ACTION = "订单服务";

    @Override
    public OrderInternalClient create(Throwable cause) {
        return new OrderInternalClient() {
            @Override
            public Result<OrderInternalDto> getOrderSummary(Long orderId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<List<OrderItemInternalDto>> listOrderItems(Long orderId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<OrderSubmitResultDto> submitOrder(OrderSubmitRequestDto request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Integer> countByMember(Long memberId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Boolean> hasPurchasedSpu(Long memberId, Long spuId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<List<PendingReviewItemDto>> listReviewablePurchaseItems(Long memberId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }
        };
    }
}
