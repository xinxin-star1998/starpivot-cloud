package cn.org.starpivot.mall.order.internal;

import cn.org.starpivot.api.mall.order.dto.PendingReviewItemDto;
import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.mall.common.MallOrderConstants;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderItem;
import cn.org.starpivot.mall.oms.mapper.OmsOrderItemMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderMemberInternalService {

    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderItemMapper omsOrderItemMapper;

    public int countByMember(Long memberId) {
        if (memberId == null) {
            return 0;
        }
        Long count = omsOrderMapper.selectCount(
                Wrappers.<OmsOrder>lambdaQuery().eq(OmsOrder::getMemberId, memberId));
        return count != null ? count.intValue() : 0;
    }

    public boolean hasPurchasedSpu(Long memberId, Long spuId) {
        if (memberId == null || spuId == null) {
            return false;
        }
        List<OmsOrder> orders = listReviewableOrders(memberId);
        if (orders.isEmpty()) {
            return false;
        }
        List<Long> orderIds = orders.stream().map(OmsOrder::getId).toList();
        Long itemCount = omsOrderItemMapper.selectCount(
                Wrappers.<OmsOrderItem>lambdaQuery()
                        .in(OmsOrderItem::getOrderId, orderIds)
                        .eq(OmsOrderItem::getSpuId, spuId));
        return itemCount != null && itemCount > 0;
    }

    public List<PendingReviewItemDto> listReviewablePurchaseItems(Long memberId) {
        if (memberId == null) {
            return List.of();
        }
        List<OmsOrder> orders = listReviewableOrders(memberId);
        if (orders.isEmpty()) {
            return List.of();
        }

        List<PendingReviewItemDto> result = new ArrayList<>();
        Set<Long> seenSpuIds = new HashSet<>();
        for (OmsOrder order : orders) {
            List<OmsOrderItem> items = omsOrderItemMapper.selectList(
                    Wrappers.<OmsOrderItem>lambdaQuery().eq(OmsOrderItem::getOrderId, order.getId()));
            for (OmsOrderItem item : items) {
                Long spuId = item.getSpuId();
                if (spuId == null || !seenSpuIds.add(spuId)) {
                    continue;
                }
                PendingReviewItemDto dto = new PendingReviewItemDto();
                dto.setSpuId(spuId);
                dto.setSkuId(item.getSkuId());
                dto.setSpuName(item.getSpuName());
                dto.setCoverImg(normalizeCover(item.getSpuPic()));
                dto.setOrderSn(order.getOrderSn());
                result.add(dto);
            }
        }
        return result;
    }

    private List<OmsOrder> listReviewableOrders(Long memberId) {
        return omsOrderMapper.selectList(
                Wrappers.<OmsOrder>lambdaQuery()
                        .eq(OmsOrder::getMemberId, memberId)
                        .in(OmsOrder::getStatus,
                                MallOrderConstants.ORDER_STATUS_DELIVERED,
                                MallOrderConstants.ORDER_STATUS_COMPLETED)
                        .orderByDesc(OmsOrder::getCreateTime));
    }

    private String normalizeCover(String pic) {
        if (!StringUtils.hasText(pic)) {
            return null;
        }
        return StorageObjectPathUtils.normalizeToObjectName(pic.trim());
    }
}
