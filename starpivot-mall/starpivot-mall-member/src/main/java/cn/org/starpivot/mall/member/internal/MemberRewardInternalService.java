package cn.org.starpivot.mall.member.internal;

import cn.org.starpivot.api.mall.order.MallOrderInternalClient;
import cn.org.starpivot.api.mall.order.dto.OrderRewardContextDto;
import cn.org.starpivot.api.member.dto.MemberOrderReturnRewardRequest;
import cn.org.starpivot.api.member.dto.MemberOrderRewardRequest;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.ums.entity.UmsGrowthChangeHistory;
import cn.org.starpivot.mall.ums.entity.UmsIntegrationChangeHistory;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import cn.org.starpivot.mall.ums.entity.UmsMemberLevel;
import cn.org.starpivot.mall.ums.mapper.UmsGrowthChangeHistoryMapper;
import cn.org.starpivot.mall.ums.mapper.UmsIntegrationChangeHistoryMapper;
import cn.org.starpivot.mall.ums.mapper.UmsMemberLevelMapper;
import cn.org.starpivot.mall.ums.mapper.UmsMemberMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MemberRewardInternalService {

    private static final int SOURCE_PAY_INTEGRATION = 3;
    private static final int SOURCE_PAY_GROWTH = 1;
    private static final int SOURCE_RETURN_CLAWBACK_INTEGRATION = 4;
    private static final int SOURCE_RETURN_CLAWBACK_GROWTH = 2;

    private final MallOrderInternalClient mallOrderInternalClient;
    private final UmsMemberMapper umsMemberMapper;
    private final UmsMemberLevelMapper umsMemberLevelMapper;
    private final UmsIntegrationChangeHistoryMapper integrationChangeHistoryMapper;
    private final UmsGrowthChangeHistoryMapper growthChangeHistoryMapper;

    @Transactional(rollbackFor = Exception.class)
    public void grantOnPaid(MemberOrderRewardRequest request) {
        if (request == null || request.getOrderId() == null) {
            return;
        }
        OrderRewardContextDto context = requireOrderRewardContext(request.getOrderId());
        if (context.getMemberId() == null) {
            return;
        }
        if (rewardAlreadyGranted(context.getOrderSn())) {
            return;
        }

        RewardTotals totals = calculateRewardTotals(context);
        if (totals.integration() <= 0 && totals.growth() <= 0) {
            patchOrderReward(context.getOrderId(), 0, 0);
            return;
        }

        UmsMember member = umsMemberMapper.selectById(context.getMemberId());
        if (member == null) {
            throw new BizException("会员不存在");
        }

        int integration = totals.integration();
        int growth = totals.growth();
        if (integration > 0) {
            // 原子更新积分，避免并发竞态
            umsMemberMapper.addIntegration(member.getId(), integration);
            writeIntegrationHistory(
                    member.getId(), integration, "购物赠送积分，订单号：" + context.getOrderSn(), SOURCE_PAY_INTEGRATION);
        }
        if (growth > 0) {
            // 原子更新成长值，避免并发竞态
            umsMemberMapper.addGrowth(member.getId(), growth);
            writeGrowthHistory(member.getId(), growth, "购物赠送成长值，订单号：" + context.getOrderSn(), SOURCE_PAY_GROWTH);
            syncMemberLevel(member.getId());
        }
        patchOrderReward(context.getOrderId(), integration, growth);
    }

    @Transactional(rollbackFor = Exception.class)
    public void clawbackOnReturn(MemberOrderReturnRewardRequest request) {
        if (request == null || request.getSkuId() == null || request.getMemberId() == null) {
            return;
        }
        int returnQty = request.getSkuCount() == null ? 0 : request.getSkuCount();
        if (returnQty <= 0) {
            return;
        }

        OrderRewardContextDto context = requireOrderRewardContext(request.getOrderId());
        OrderRewardContextDto.OrderItemLine item = findItemLine(context, request.getSkuId());
        if (item == null || item.getSpuId() == null) {
            return;
        }
        int orderQty = item.getSkuQuantity() == null ? 0 : item.getSkuQuantity();
        if (orderQty <= 0) {
            return;
        }
        int clawQty = Math.min(returnQty, orderQty);
        OrderRewardContextDto.SpuBoundsLine bounds = resolveBounds(context, item.getSpuId());
        if (bounds == null) {
            return;
        }

        int clawIntegration = scalePoints(bounds.getBuyBounds(), clawQty);
        int clawGrowth = scalePoints(bounds.getGrowBounds(), clawQty);
        if (clawIntegration <= 0 && clawGrowth <= 0) {
            return;
        }

        UmsMember member = umsMemberMapper.selectById(request.getMemberId());
        if (member == null) {
            return;
        }

        int earnedIntegration = request.getOrderIntegration() == null ? 0 : request.getOrderIntegration();
        int earnedGrowth = request.getOrderGrowth() == null ? 0 : request.getOrderGrowth();
        int newIntegration = earnedIntegration;
        int newGrowth = earnedGrowth;

        if (clawIntegration > 0) {
            // 原子减少积分，不低于 0
            umsMemberMapper.deductIntegrationClampZero(member.getId(), clawIntegration);
            writeIntegrationHistory(
                    member.getId(),
                    -clawIntegration,
                    "退货回滚积分，订单号：" + request.getOrderSn(),
                    SOURCE_RETURN_CLAWBACK_INTEGRATION);
            newIntegration = Math.max(earnedIntegration - clawIntegration, 0);
        }
        if (clawGrowth > 0) {
            // 原子减少成长值，不低于 0
            umsMemberMapper.deductGrowthClampZero(member.getId(), clawGrowth);
            writeGrowthHistory(
                    member.getId(),
                    -clawGrowth,
                    "退货回滚成长值，订单号：" + request.getOrderSn(),
                    SOURCE_RETURN_CLAWBACK_GROWTH);
            newGrowth = Math.max(earnedGrowth - clawGrowth, 0);
            syncMemberLevel(member.getId());
        }
        patchOrderReward(request.getOrderId(), newIntegration, newGrowth);
    }

    private OrderRewardContextDto requireOrderRewardContext(Long orderId) {
        Result<OrderRewardContextDto> result = mallOrderInternalClient.getOrderRewardContext(orderId);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new BizException(result != null ? result.getMessage() : "订单奖励上下文查询失败");
        }
        return result.getData();
    }

    private void patchOrderReward(Long orderId, Integer integration, Integer growth) {
        Result<Void> result = mallOrderInternalClient.patchOrderReward(orderId, integration, growth);
        if (result == null || !result.isSuccess()) {
            throw new BizException(result != null ? result.getMessage() : "更新订单奖励失败");
        }
    }

    private OrderRewardContextDto.OrderItemLine findItemLine(OrderRewardContextDto context, Long skuId) {
        if (context.getItems() == null || skuId == null) {
            return null;
        }
        return context.getItems().stream()
                .filter(item -> skuId.equals(item.getSkuId()))
                .findFirst()
                .orElse(null);
    }

    private OrderRewardContextDto.SpuBoundsLine resolveBounds(OrderRewardContextDto context, Long spuId) {
        if (context.getSpuBounds() == null || spuId == null) {
            return null;
        }
        OrderRewardContextDto.SpuBoundsLine bounds = context.getSpuBounds().get(spuId);
        return isActive(bounds) ? bounds : null;
    }

    private RewardTotals calculateRewardTotals(OrderRewardContextDto context) {
        if (CollectionUtils.isEmpty(context.getItems()) || context.getSpuBounds() == null) {
            return new RewardTotals(0, 0);
        }
        int integration = 0;
        int growth = 0;
        for (OrderRewardContextDto.OrderItemLine item : context.getItems()) {
            if (item.getSpuId() == null) {
                continue;
            }
            OrderRewardContextDto.SpuBoundsLine bounds = context.getSpuBounds().get(item.getSpuId());
            if (!isActive(bounds)) {
                continue;
            }
            int qty = item.getSkuQuantity() == null ? 0 : item.getSkuQuantity();
            if (qty <= 0) {
                continue;
            }
            integration += scalePoints(bounds.getBuyBounds(), qty);
            growth += scalePoints(bounds.getGrowBounds(), qty);
        }
        return new RewardTotals(integration, growth);
    }

    private boolean isActive(OrderRewardContextDto.SpuBoundsLine bounds) {
        if (bounds == null) {
            return false;
        }
        return bounds.getWork() == null || bounds.getWork() != 0;
    }

    private int scalePoints(BigDecimal perUnit, int quantity) {
        if (perUnit == null || perUnit.compareTo(BigDecimal.ZERO) <= 0 || quantity <= 0) {
            return 0;
        }
        return perUnit.multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.DOWN).intValue();
    }

    private boolean rewardAlreadyGranted(String orderSn) {
        if (orderSn == null) {
            return false;
        }
        // 使用精确后缀匹配替代 LIKE '%orderSn%'，避免 orderSn 子串误判
        // note 格式为 "购物赠送积分，订单号：SP12345"，以 "订单号：" + orderSn 结尾
        String noteSuffix = "订单号：" + orderSn;
        long integrationCount = integrationChangeHistoryMapper.selectCount(
                Wrappers.<UmsIntegrationChangeHistory>lambdaQuery()
                        .eq(UmsIntegrationChangeHistory::getSourceType, SOURCE_PAY_INTEGRATION)
                        .apply("note LIKE CONCAT('%', {0})", noteSuffix));
        long growthCount = growthChangeHistoryMapper.selectCount(
                Wrappers.<UmsGrowthChangeHistory>lambdaQuery()
                        .eq(UmsGrowthChangeHistory::getSourceType, SOURCE_PAY_GROWTH)
                        .apply("note LIKE CONCAT('%', {0})", noteSuffix));
        return integrationCount > 0 || growthCount > 0;
    }

    /**
     * 按最新成长值同步等级，仅更新 level_id，避免覆盖并发积分/成长值写入。
     */
    private void syncMemberLevel(Long memberId) {
        UmsMember member = umsMemberMapper.selectById(memberId);
        if (member == null) {
            return;
        }
        Long matchedLevelId = resolveLevelId(member.getGrowth());
        if (matchedLevelId != null && !Objects.equals(matchedLevelId, member.getLevelId())) {
            umsMemberMapper.updateLevelId(memberId, matchedLevelId);
        }
    }

    private Long resolveLevelId(Integer growthValue) {
        int growth = growthValue == null ? 0 : growthValue;
        List<UmsMemberLevel> levels = umsMemberLevelMapper.selectList(
                Wrappers.<UmsMemberLevel>lambdaQuery()
                        .orderByDesc(UmsMemberLevel::getGrowthPoint)
                        .orderByDesc(UmsMemberLevel::getId));
        for (UmsMemberLevel level : levels) {
            if (level.getGrowthPoint() == null) {
                continue;
            }
            if (growth >= level.getGrowthPoint()) {
                return level.getId();
            }
        }
        return null;
    }

    private void writeIntegrationHistory(Long memberId, int changeCount, String note, int sourceType) {
        UmsIntegrationChangeHistory history = new UmsIntegrationChangeHistory();
        history.setMemberId(memberId);
        history.setChangeCount(changeCount);
        history.setNote(note);
        history.setSourceType(sourceType);
        history.setCreateTime(LocalDateTime.now());
        integrationChangeHistoryMapper.insert(history);
    }

    private void writeGrowthHistory(Long memberId, int changeCount, String note, int sourceType) {
        UmsGrowthChangeHistory history = new UmsGrowthChangeHistory();
        history.setMemberId(memberId);
        history.setChangeCount(changeCount);
        history.setNote(note);
        history.setSourceType(sourceType);
        history.setCreateTime(LocalDateTime.now());
        growthChangeHistoryMapper.insert(history);
    }

    private record RewardTotals(int integration, int growth) {}
}
