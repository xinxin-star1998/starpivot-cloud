package cn.org.starpivot.api.mall.order;

import cn.org.starpivot.api.fallback.OrderInternalClientFallbackFactory;
import cn.org.starpivot.api.mall.order.dto.OrderInternalDto;
import cn.org.starpivot.api.mall.order.dto.OrderItemInternalDto;
import cn.org.starpivot.api.mall.order.dto.OrderSubmitRequestDto;
import cn.org.starpivot.api.mall.order.dto.OrderSubmitResultDto;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 订单服务内部接口（供仓储、营销 BFF 等拉取/提交订单）。
 */
@FeignClient(
        name = "starpivot-mall-order",
        contextId = "orderInternalClient",
        path = "/api/${starpivot.api.version:v1}",
        fallbackFactory = OrderInternalClientFallbackFactory.class)
public interface OrderInternalClient {

    @GetMapping("/internal/mall/order/{orderId}/summary")
    Result<OrderInternalDto> getOrderSummary(@PathVariable("orderId") Long orderId);

    @GetMapping("/internal/mall/order/{orderId}/items")
    Result<List<OrderItemInternalDto>> listOrderItems(@PathVariable("orderId") Long orderId);

    @PostMapping("/internal/mall/order/submit")
    Result<OrderSubmitResultDto> submitOrder(@RequestBody OrderSubmitRequestDto request);
}
