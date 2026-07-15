package cn.org.starpivot.mall.order.controller.internal;

import cn.org.starpivot.api.mall.order.dto.OrderRewardContextDto;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.order.internal.OrderInternalService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Hidden
@RestController
@RequestMapping("/internal/mall/order")
@RequiredArgsConstructor
public class MallOrderInternalController {

    private final OrderInternalService orderInternalService;

    @GetMapping("/{orderId}/reward-context")
    public Result<OrderRewardContextDto> getOrderRewardContext(@PathVariable("orderId") Long orderId) {
        OrderRewardContextDto dto = orderInternalService.getOrderRewardContext(orderId);
        if (dto == null) {
            return Result.notFound("订单不存在");
        }
        return Result.success(dto);
    }

    @PutMapping("/{orderId}/reward")
    public Result<Void> patchOrderReward(
            @PathVariable("orderId") Long orderId,
            @RequestParam("integration") Integer integration,
            @RequestParam("growth") Integer growth) {
        if (!orderInternalService.patchOrderReward(orderId, integration, growth)) {
            return Result.notFound("订单不存在");
        }
        return Result.success();
    }
}
