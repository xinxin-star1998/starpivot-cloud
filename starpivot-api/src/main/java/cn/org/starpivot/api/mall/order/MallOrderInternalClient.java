package cn.org.starpivot.api.mall.order;

import cn.org.starpivot.api.fallback.MallOrderInternalClientFallbackFactory;
import cn.org.starpivot.api.mall.order.dto.OrderRewardContextDto;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 商城主服务订单内部接口（供 member 服务计算奖励时拉取订单上下文）。
 */
@FeignClient(
        name = "starpivot-mall-order",
        contextId = "mallOrderInternalClient",
        path = "/api/${starpivot.api.version:v1}",
        fallbackFactory = MallOrderInternalClientFallbackFactory.class)
public interface MallOrderInternalClient {

    @GetMapping("/internal/mall/order/{orderId}/reward-context")
    Result<OrderRewardContextDto> getOrderRewardContext(@PathVariable("orderId") Long orderId);

    @PutMapping("/internal/mall/order/{orderId}/reward")
    Result<Void> patchOrderReward(
            @PathVariable("orderId") Long orderId,
            @RequestParam("integration") Integer integration,
            @RequestParam("growth") Integer growth);
}
