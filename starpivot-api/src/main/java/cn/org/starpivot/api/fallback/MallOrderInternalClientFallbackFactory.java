package cn.org.starpivot.api.fallback;

import cn.org.starpivot.api.mall.order.MallOrderInternalClient;
import cn.org.starpivot.api.mall.order.dto.OrderRewardContextDto;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FallbackFactory;

public class MallOrderInternalClientFallbackFactory implements FallbackFactory<MallOrderInternalClient> {

    private static final String ACTION = "商城订单服务";

    @Override
    public MallOrderInternalClient create(Throwable cause) {
        return new MallOrderInternalClient() {
            @Override
            public Result<OrderRewardContextDto> getOrderRewardContext(Long orderId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Void> patchOrderReward(Long orderId, Integer integration, Integer growth) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }
        };
    }
}
