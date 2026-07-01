package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderItem;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;
import cn.org.starpivot.mall.oms.mapper.OmsOrderItemMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.portal.service.PortalMemberRewardService;
import cn.org.starpivot.mall.sms.entity.SmsSpuBounds;
import cn.org.starpivot.mall.sms.mapper.SmsSpuBoundsMapper;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortalMemberRewardServiceImpl implements PortalMemberRewardService {

    private static final int SOURCE_PAY_INTEGRATION = 3;
    private static final int SOURCE_PAY_GROWTH = 1;
    private static final int SOURCE_RETURN_CLAWBACK_INTEGRATION = 4;
    private static final int SOURCE_RETURN_CLAWBACK_GROWTH = 2;

    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderItemMapper omsOrderItemMapper;
    private final SmsSpuBoundsMapper smsSpuBoundsMapper;
    private final UmsMemberMapper umsMemberMapper;
    private final UmsMemberLevelMapper umsMemberLevelMapper;
    private final UmsIntegrationChangeHistoryMapper integrationChangeHistoryMapper;
    private final UmsGrowthChangeHistoryMapper growthChangeHistoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantOnPaid(OmsOrder order) {
        if (order == null || order.getId() == null || order.getMemberId() == null) {
            return;
        }
        if (rewardAlreadyGranted(order.getOrderSn())) {
            return;
        }

        RewardTotals totals = calculateRewardTotals(order.getId());
        if (totals.integration() <= 0 && totals.growth() <= 0) {
            patchOrderReward(order.getId(), 0, 0);
            return;
        }

        UmsMember member = umsMemberMapper.selectById(order.getMemberId());
        if (member == null) {
            throw new BizException("会员不存在");
        }

        int integration = totals.integration();
        int growth = totals.growth();
        if (integration > 0) {
            int current = member.getIntegration() == null ? 0 : member.getIntegration();
            member.setIntegration(current + integration);
            writeIntegrationHistory(
                    member.getId(), integration, "购物赠送积分，订单号：" + order.getOrderSn(), SOURCE_PAY_INTEGRATION);
        }
        if (growth > 0) {
            int currentGrowth = member.getGrowth() == null ? 0 : member.getGrowth();
            member.setGrowth(currentGrowth + growth);
            writeGrowthHistory(member.getId(), growth, "购物赠送成长值，订单号：" + order.getOrderSn(), SOURCE_PAY_GROWTH);
            upgradeLevelIfNeeded(member);
        }
        umsMemberMapper.updateById(member);
        patchOrderReward(order.getId(), integration, growth);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clawbackOnReturn(OmsOrder order, OmsOrderReturnApply apply) {
        if (order == null || apply == null || apply.getSkuId() == null) {
            return;
        }
        int returnQty = apply.getSkuCount() == null ? 0 : apply.getSkuCount();
        if (returnQty <= 0) {
            return;
        }
        OmsOrderItem item = omsOrderItemMapper.selectOne(
                Wrappers.<OmsOrderItem>lambdaQuery()
                        .eq(OmsOrderItem::getOrderId, order.getId())
                        .eq(OmsOrderItem::getSkuId, apply.getSkuId())
                        .last("LIMIT 1"));
        if (item == null || item.getSpuId() == null) {
            return;
        }
        int orderQty = item.getSkuQuantity() == null ? 0 : item.getSkuQuantity();
        if (orderQty <= 0) {
            return;
        }
        int clawQty = Math.min(returnQty, orderQty);
        SmsSpuBounds bounds = loadActiveBounds(item.getSpuId());
        if (bounds == null) {
            return;
        }

        int clawIntegration = scalePoints(bounds.getBuyBounds(), clawQty);
        int clawGrowth = scalePoints(bounds.getGrowBounds(), clawQty);
        if (clawIntegration <= 0 && clawGrowth <= 0) {
            return;
        }

        UmsMember member = umsMemberMapper.selectById(order.getMemberId());
        if (member == null) {
            return;
        }

        int earnedIntegration = order.getIntegration() == null ? 0 : order.getIntegration();
        int earnedGrowth = order.getGrowth() == null ? 0 : order.getGrowth();
        int newIntegration = earnedIntegration;
        int newGrowth = earnedGrowth;

        if (clawIntegration > 0) {
            int current = member.getIntegration() == null ? 0 : member.getIntegration();
            member.setIntegration(Math.max(current - clawIntegration, 0));
            writeIntegrationHistory(
                    member.getId(),
                    -clawIntegration,
                    "退货回滚积分，订单号：" + order.getOrderSn(),
                    SOURCE_RETURN_CLAWBACK_INTEGRATION);
            newIntegration = Math.max(earnedIntegration - clawIntegration, 0);
        }
        if (clawGrowth > 0) {
            int currentGrowth = member.getGrowth() == null ? 0 : member.getGrowth();
            member.setGrowth(Math.max(currentGrowth - clawGrowth, 0));
            writeGrowthHistory(
                    member.getId(),
                    -clawGrowth,
                    "退货回滚成长值，订单号：" + order.getOrderSn(),
                    SOURCE_RETURN_CLAWBACK_GROWTH);
            newGrowth = Math.max(earnedGrowth - clawGrowth, 0);
            upgradeLevelIfNeeded(member);
        }
        umsMemberMapper.updateById(member);
        patchOrderReward(order.getId(), newIntegration, newGrowth);
        order.setIntegration(newIntegration);
        order.setGrowth(newGrowth);
    }

    private RewardTotals calculateRewardTotals(Long orderId) {
        List<OmsOrderItem> items = omsOrderItemMapper.selectList(
                Wrappers.<OmsOrderItem>lambdaQuery().eq(OmsOrderItem::getOrderId, orderId));
        if (CollectionUtils.isEmpty(items)) {
            return new RewardTotals(0, 0);
        }
        Set<Long> spuIds = items.stream()
                .map(OmsOrderItem::getSpuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, SmsSpuBounds> boundsMap = loadBoundsMap(spuIds);

        int integration = 0;
        int growth = 0;
        for (OmsOrderItem item : items) {
            if (item.getSpuId() == null) {
                continue;
            }
            SmsSpuBounds bounds = boundsMap.get(item.getSpuId());
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

    private Map<Long, SmsSpuBounds> loadBoundsMap(Set<Long> spuIds) {
        if (CollectionUtils.isEmpty(spuIds)) {
            return Map.of();
        }
        return smsSpuBoundsMapper.selectList(
                        Wrappers.<SmsSpuBounds>lambdaQuery().in(SmsSpuBounds::getSpuId, spuIds))
                .stream()
                .collect(Collectors.toMap(SmsSpuBounds::getSpuId, b -> b, (a, b) -> a));
    }

    private SmsSpuBounds loadActiveBounds(Long spuId) {
        SmsSpuBounds bounds = smsSpuBoundsMapper.selectOne(
                Wrappers.<SmsSpuBounds>lambdaQuery().eq(SmsSpuBounds::getSpuId, spuId).last("LIMIT 1"));
        return isActive(bounds) ? bounds : null;
    }

    private boolean isActive(SmsSpuBounds bounds) {
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
        long integrationCount = integrationChangeHistoryMapper.selectCount(
                Wrappers.<UmsIntegrationChangeHistory>lambdaQuery()
                        .eq(UmsIntegrationChangeHistory::getSourceType, SOURCE_PAY_INTEGRATION)
                        .like(UmsIntegrationChangeHistory::getNote, orderSn));
        long growthCount = growthChangeHistoryMapper.selectCount(
                Wrappers.<UmsGrowthChangeHistory>lambdaQuery()
                        .eq(UmsGrowthChangeHistory::getSourceType, SOURCE_PAY_GROWTH)
                        .like(UmsGrowthChangeHistory::getNote, orderSn));
        return integrationCount > 0 || growthCount > 0;
    }

    private void upgradeLevelIfNeeded(UmsMember member) {
        int growth = member.getGrowth() == null ? 0 : member.getGrowth();
        List<UmsMemberLevel> levels = umsMemberLevelMapper.selectList(
                Wrappers.<UmsMemberLevel>lambdaQuery()
                        .orderByDesc(UmsMemberLevel::getGrowthPoint)
                        .orderByDesc(UmsMemberLevel::getId));
        for (UmsMemberLevel level : levels) {
            if (level.getGrowthPoint() == null) {
                continue;
            }
            if (growth >= level.getGrowthPoint()) {
                if (!Objects.equals(level.getId(), member.getLevelId())) {
                    member.setLevelId(level.getId());
                }
                break;
            }
        }
    }

    private void patchOrderReward(Long orderId, Integer integration, Integer growth) {
        OmsOrder patch = new OmsOrder();
        patch.setId(orderId);
        patch.setIntegration(integration);
        patch.setGrowth(growth);
        omsOrderMapper.updateById(patch);
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
