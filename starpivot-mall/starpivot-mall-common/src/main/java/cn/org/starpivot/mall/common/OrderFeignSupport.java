package cn.org.starpivot.mall.common;

import cn.org.starpivot.api.mall.order.OrderInternalClient;
import cn.org.starpivot.api.mall.order.dto.OrderInternalDto;
import cn.org.starpivot.api.mall.order.dto.OrderItemInternalDto;
import cn.org.starpivot.api.mall.order.dto.PendingReviewItemDto;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderFeignSupport {

    private static final String ACTION = "订单服务";

    private final OrderInternalClient orderInternalClient;

    public OrderInternalDto requireOrderSummary(Long orderId) {
        return unwrap(orderInternalClient.getOrderSummary(orderId), "订单不存在");
    }

    public List<OrderItemInternalDto> requireOrderItems(Long orderId) {
        List<OrderItemInternalDto> items = unwrap(orderInternalClient.listOrderItems(orderId), "订单明细不存在");
        return items == null ? List.of() : items;
    }

    public int countByMember(Long memberId) {
        Integer count = unwrap(orderInternalClient.countByMember(memberId), "订单数量加载失败");
        return count == null ? 0 : count;
    }

    public boolean hasPurchasedSpu(Long memberId, Long spuId) {
        Boolean purchased = unwrap(orderInternalClient.hasPurchasedSpu(memberId, spuId), "购买记录查询失败");
        return Boolean.TRUE.equals(purchased);
    }

    public List<PendingReviewItemDto> listReviewablePurchaseItems(Long memberId) {
        List<PendingReviewItemDto> items = unwrap(
                orderInternalClient.listReviewablePurchaseItems(memberId), "待评价商品加载失败");
        return items == null ? List.of() : items;
    }

    private <T> T unwrap(Result<T> result, String notFoundMessage) {
        if (result == null) {
            throw new BizException(ACTION + "不可用");
        }
        if (!result.isSuccess()) {
            if (result.getCode() == ErrorCode.NOT_FOUND) {
                throw new BizException(notFoundMessage);
            }
            throw new BizException(result.getMessage());
        }
        return result.getData();
    }
}
