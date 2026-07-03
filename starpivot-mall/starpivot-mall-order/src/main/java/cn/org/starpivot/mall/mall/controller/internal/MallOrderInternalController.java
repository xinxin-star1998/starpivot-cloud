package cn.org.starpivot.mall.mall.controller.internal;

import cn.org.starpivot.api.mall.order.dto.OrderRewardContextDto;
import cn.org.starpivot.api.mall.promotion.PromotionInternalClient;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderItem;
import cn.org.starpivot.mall.oms.mapper.OmsOrderItemMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Hidden
@RestController
@RequestMapping("/internal/mall/order")
@RequiredArgsConstructor
public class MallOrderInternalController {

    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderItemMapper omsOrderItemMapper;
    private final PromotionInternalClient promotionInternalClient;

    @GetMapping("/{orderId}/reward-context")
    public Result<OrderRewardContextDto> getOrderRewardContext(@PathVariable("orderId") Long orderId) {
        OmsOrder order = omsOrderMapper.selectById(orderId);
        if (order == null) {
            return Result.notFound("订单不存在");
        }

        List<OmsOrderItem> items = omsOrderItemMapper.selectList(
                Wrappers.<OmsOrderItem>lambdaQuery().eq(OmsOrderItem::getOrderId, orderId));

        OrderRewardContextDto dto = new OrderRewardContextDto();
        dto.setOrderId(order.getId());
        dto.setOrderSn(order.getOrderSn());
        dto.setMemberId(order.getMemberId());
        dto.setIntegration(order.getIntegration());
        dto.setGrowth(order.getGrowth());
        dto.setItems(items.stream().map(this::toItemLine).collect(Collectors.toList()));
        dto.setSpuBounds(loadBoundsMap(items));
        return Result.success(dto);
    }

    @PutMapping("/{orderId}/reward")
    public Result<Void> patchOrderReward(
            @PathVariable("orderId") Long orderId,
            @RequestParam("integration") Integer integration,
            @RequestParam("growth") Integer growth) {
        OmsOrder existing = omsOrderMapper.selectById(orderId);
        if (existing == null) {
            return Result.notFound("订单不存在");
        }
        OmsOrder patch = new OmsOrder();
        patch.setId(orderId);
        patch.setIntegration(integration);
        patch.setGrowth(growth);
        omsOrderMapper.updateById(patch);
        return Result.success();
    }

    private OrderRewardContextDto.OrderItemLine toItemLine(OmsOrderItem item) {
        OrderRewardContextDto.OrderItemLine line = new OrderRewardContextDto.OrderItemLine();
        line.setSpuId(item.getSpuId());
        line.setSkuId(item.getSkuId());
        line.setSkuQuantity(item.getSkuQuantity());
        return line;
    }

    private Map<Long, OrderRewardContextDto.SpuBoundsLine> loadBoundsMap(List<OmsOrderItem> items) {
        if (CollectionUtils.isEmpty(items)) {
            return Map.of();
        }
        Set<Long> spuIds = items.stream()
                .map(OmsOrderItem::getSpuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (spuIds.isEmpty()) {
            return Map.of();
        }
        Result<Map<Long, OrderRewardContextDto.SpuBoundsLine>> result =
                promotionInternalClient.batchSpuBounds(spuIds);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            return Map.of();
        }
        return result.getData();
    }
}
