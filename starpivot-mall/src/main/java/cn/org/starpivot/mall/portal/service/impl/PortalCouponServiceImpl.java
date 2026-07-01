package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.pms.entity.PmsSkuInfo;
import cn.org.starpivot.mall.pms.entity.PmsSpuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsSkuInfoMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuInfoMapper;
import cn.org.starpivot.mall.portal.domain.bo.PortalCouponTrialBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderItemBo;
import cn.org.starpivot.mall.portal.domain.vo.*;
import cn.org.starpivot.mall.portal.service.PortalCartService;
import cn.org.starpivot.mall.portal.service.PortalCouponService;
import cn.org.starpivot.mall.portal.service.PortalOrderPriceService;
import cn.org.starpivot.mall.sms.entity.SmsCoupon;
import cn.org.starpivot.mall.sms.entity.SmsCouponHistory;
import cn.org.starpivot.mall.sms.entity.SmsCouponSpuCategoryRelation;
import cn.org.starpivot.mall.sms.entity.SmsCouponSpuRelation;
import cn.org.starpivot.mall.sms.mapper.SmsCouponHistoryMapper;
import cn.org.starpivot.mall.sms.mapper.SmsCouponMapper;
import cn.org.starpivot.mall.sms.mapper.SmsCouponSpuCategoryRelationMapper;
import cn.org.starpivot.mall.sms.mapper.SmsCouponSpuRelationMapper;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import cn.org.starpivot.mall.ums.mapper.UmsMemberMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortalCouponServiceImpl implements PortalCouponService {

    private static final int HISTORY_UNUSED = 0;
    private static final int HISTORY_USED = 1;
    private static final int HISTORY_EXPIRED = 2;
    private static final int GET_TYPE_MEMBER = 0;
    private static final int COUPON_PUBLISHED = 1;
    private static final int COUPON_USE_ALL = 0;
    private static final int COUPON_USE_CATEGORY = 1;
    private static final int COUPON_USE_SPU = 2;

    private static final DateTimeFormatter COUPON_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final SmsCouponHistoryMapper smsCouponHistoryMapper;
    private final SmsCouponMapper smsCouponMapper;
    private final SmsCouponSpuRelationMapper smsCouponSpuRelationMapper;
    private final SmsCouponSpuCategoryRelationMapper smsCouponSpuCategoryRelationMapper;
    private final PortalCartService portalCartService;
    private final PmsSkuInfoMapper pmsSkuInfoMapper;
    private final PmsSpuInfoMapper pmsSpuInfoMapper;
    private final PortalOrderPriceService portalOrderPriceService;
    private final UmsMemberMapper umsMemberMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PortalClaimableCouponVo> listClaimable(Long memberId) {
        LocalDateTime now = LocalDateTime.now();
        List<SmsCoupon> coupons = smsCouponMapper.selectList(
                Wrappers.<SmsCoupon>lambdaQuery().eq(SmsCoupon::getPublish, COUPON_PUBLISHED));
        if (coupons.isEmpty()) {
            return List.of();
        }
        List<PortalClaimableCouponVo> result = new ArrayList<>();
        for (SmsCoupon coupon : coupons) {
            if (!isInEnableWindow(coupon, now) || !hasStock(coupon)) {
                continue;
            }
            int received = countMemberReceived(memberId, coupon.getId());
            PortalClaimableCouponVo vo = new PortalClaimableCouponVo();
            vo.setCouponId(coupon.getId());
            vo.setCouponName(coupon.getCouponName());
            vo.setAmount(coupon.getAmount());
            vo.setMinPoint(coupon.getMinPoint());
            vo.setUseType(coupon.getUseType());
            vo.setStartTime(coupon.getStartTime());
            vo.setEndTime(coupon.getEndTime());
            vo.setEnableEndTime(coupon.getEnableEndTime());
            vo.setPerLimit(coupon.getPerLimit());
            vo.setReceivedCount(received);
            vo.setCanReceive(canMemberReceive(coupon, received));
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortalMyCouponVo> listMine(Long memberId, Integer status) {
        List<SmsCouponHistory> histories = smsCouponHistoryMapper.selectList(
                Wrappers.<SmsCouponHistory>lambdaQuery()
                        .eq(SmsCouponHistory::getMemberId, memberId)
                        .orderByDesc(SmsCouponHistory::getCreateTime));
        if (histories.isEmpty()) {
            return List.of();
        }
        Map<Long, SmsCoupon> couponMap = loadCouponsByIds(
                histories.stream().map(SmsCouponHistory::getCouponId).collect(Collectors.toSet()));
        LocalDateTime now = LocalDateTime.now();
        List<PortalMyCouponVo> result = new ArrayList<>();
        for (SmsCouponHistory history : histories) {
            SmsCoupon coupon = couponMap.get(history.getCouponId());
            if (coupon == null) {
                continue;
            }
            int resolvedStatus = resolveHistoryStatus(history, coupon, now);
            if (status != null && !status.equals(resolvedStatus)) {
                continue;
            }
            PortalMyCouponVo vo = new PortalMyCouponVo();
            vo.setHistoryId(history.getId());
            vo.setCouponId(coupon.getId());
            vo.setCouponName(coupon.getCouponName());
            vo.setAmount(coupon.getAmount());
            vo.setMinPoint(coupon.getMinPoint());
            vo.setUseType(coupon.getUseType());
            vo.setStatus(resolvedStatus);
            vo.setEndTime(coupon.getEndTime());
            vo.setCreateTime(history.getCreateTime());
            vo.setUseTime(history.getUseTime());
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long receive(Long memberId, Long couponId) {
        if (couponId == null) {
            throw new BizException("优惠券ID不能为空");
        }
        UmsMember member = umsMemberMapper.selectById(memberId);
        if (member == null) {
            throw new BizException("会员不存在");
        }
        SmsCoupon coupon = smsCouponMapper.selectById(couponId);
        if (coupon == null || !Integer.valueOf(COUPON_PUBLISHED).equals(coupon.getPublish())) {
            throw new BizException("优惠券不存在或未发布");
        }
        LocalDateTime now = LocalDateTime.now();
        if (!isInEnableWindow(coupon, now)) {
            throw new BizException("不在优惠券领取时间内");
        }
        if (!hasStock(coupon)) {
            throw new BizException("优惠券已被领完");
        }
        int received = countMemberReceived(memberId, couponId);
        if (!canMemberReceive(coupon, received)) {
            throw new BizException("已达到该优惠券领取上限");
        }

        SmsCouponHistory history = new SmsCouponHistory();
        history.setCouponId(couponId);
        history.setMemberId(memberId);
        history.setMemberNickName(member.getNickname());
        history.setGetType(GET_TYPE_MEMBER);
        history.setCreateTime(now);
        history.setUseType(HISTORY_UNUSED);
        smsCouponHistoryMapper.insert(history);

        coupon.setReceiveCount(coupon.getReceiveCount() == null ? 1 : coupon.getReceiveCount() + 1);
        smsCouponMapper.updateById(coupon);
        return history.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortalMemberCouponVo> listUsable(Long memberId, PortalCouponTrialBo trialBo) {
        OrderScope scope = resolveOrderScope(memberId, trialBo);
        List<SmsCouponHistory> histories = smsCouponHistoryMapper.selectList(
                Wrappers.<SmsCouponHistory>lambdaQuery()
                        .eq(SmsCouponHistory::getMemberId, memberId)
                        .eq(SmsCouponHistory::getUseType, HISTORY_UNUSED)
                        .and(w -> w.isNull(SmsCouponHistory::getOrderId).or().eq(SmsCouponHistory::getOrderId, 0)));
        if (histories.isEmpty()) {
            return List.of();
        }
        Map<Long, SmsCoupon> couponMap = loadCouponsByIds(
                histories.stream().map(SmsCouponHistory::getCouponId).collect(Collectors.toSet()));
        List<PortalMemberCouponVo> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (SmsCouponHistory history : histories) {
            SmsCoupon coupon = couponMap.get(history.getCouponId());
            if (coupon == null || !isCouponActive(coupon, now)) {
                continue;
            }
            if (!matchesScope(coupon, scope)) {
                continue;
            }
            PortalMemberCouponVo vo = new PortalMemberCouponVo();
            vo.setHistoryId(history.getId());
            vo.setCouponId(coupon.getId());
            vo.setCouponName(coupon.getCouponName());
            vo.setAmount(coupon.getAmount());
            vo.setMinPoint(coupon.getMinPoint());
            vo.setUseType(coupon.getUseType());
            vo.setEndTime(coupon.getEndTime());
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortalCheckoutCouponVo> listCheckoutCoupons(Long memberId, PortalCouponTrialBo trialBo) {
        OrderScope scope = resolveOrderScope(memberId, trialBo);
        List<SmsCouponHistory> histories = smsCouponHistoryMapper.selectList(
                Wrappers.<SmsCouponHistory>lambdaQuery()
                        .eq(SmsCouponHistory::getMemberId, memberId)
                        .eq(SmsCouponHistory::getUseType, HISTORY_UNUSED)
                        .and(w -> w.isNull(SmsCouponHistory::getOrderId).or().eq(SmsCouponHistory::getOrderId, 0)));
        if (histories.isEmpty()) {
            return List.of();
        }
        Map<Long, SmsCoupon> couponMap = loadCouponsByIds(
                histories.stream().map(SmsCouponHistory::getCouponId).collect(Collectors.toSet()));
        LocalDateTime now = LocalDateTime.now();
        List<PortalCheckoutCouponVo> result = new ArrayList<>();
        for (SmsCouponHistory history : histories) {
            SmsCoupon coupon = couponMap.get(history.getCouponId());
            if (coupon == null) {
                continue;
            }
            if (!matchesScope(coupon, scope)) {
                continue;
            }
            PortalCheckoutCouponVo vo = toCheckoutCouponVo(history, coupon);
            if (isCouponActive(coupon, now)) {
                vo.setUsable(Boolean.TRUE);
            } else {
                vo.setUsable(Boolean.FALSE);
                vo.setUnusableReason(buildInactiveReason(coupon, now));
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(
            Long memberId,
            Long couponHistoryId,
            PortalCouponTrialBo trialBo) {
        if (couponHistoryId == null) {
            return BigDecimal.ZERO;
        }
        SmsCouponHistory history = requireUnusedHistory(memberId, couponHistoryId);
        SmsCoupon coupon = smsCouponMapper.selectById(history.getCouponId());
        if (coupon == null) {
            throw new BizException("优惠券不存在");
        }
        if (!isCouponActive(coupon, LocalDateTime.now())) {
            throw new BizException("优惠券不在有效期内");
        }
        OrderScope scope = resolveOrderScope(memberId, trialBo);
        if (!matchesScope(coupon, scope)) {
            throw new BizException("订单不满足优惠券使用条件");
        }
        return coupon.getAmount() == null ? BigDecimal.ZERO : coupon.getAmount();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockToOrder(Long couponHistoryId, Long memberId, Long orderId, String orderSn) {
        if (couponHistoryId == null) {
            return;
        }
        SmsCouponHistory history = requireUnusedHistory(memberId, couponHistoryId);
        history.setOrderId(orderId);
        history.setOrderSn(orderSn);
        smsCouponHistoryMapper.updateById(history);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmUsed(Long orderId) {
        if (orderId == null) {
            return;
        }
        SmsCouponHistory history = smsCouponHistoryMapper.selectOne(
                Wrappers.<SmsCouponHistory>lambdaQuery()
                        .eq(SmsCouponHistory::getOrderId, orderId)
                        .eq(SmsCouponHistory::getUseType, HISTORY_UNUSED)
                        .last("LIMIT 1"));
        if (history == null) {
            return;
        }
        history.setUseType(HISTORY_USED);
        history.setUseTime(LocalDateTime.now());
        smsCouponHistoryMapper.updateById(history);
        SmsCoupon coupon = smsCouponMapper.selectById(history.getCouponId());
        if (coupon != null) {
            coupon.setUseCount(coupon.getUseCount() == null ? 1 : coupon.getUseCount() + 1);
            smsCouponMapper.updateById(coupon);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseByOrder(Long orderId) {
        if (orderId == null) {
            return;
        }
        SmsCouponHistory history = smsCouponHistoryMapper.selectOne(
                Wrappers.<SmsCouponHistory>lambdaQuery()
                        .eq(SmsCouponHistory::getOrderId, orderId)
                        .eq(SmsCouponHistory::getUseType, HISTORY_UNUSED)
                        .last("LIMIT 1"));
        if (history == null) {
            return;
        }
        history.setOrderId(null);
        history.setOrderSn(null);
        smsCouponHistoryMapper.updateById(history);
    }

    @Override
    public Long resolveCouponId(Long couponHistoryId) {
        if (couponHistoryId == null) {
            return null;
        }
        SmsCouponHistory history = smsCouponHistoryMapper.selectById(couponHistoryId);
        return history != null ? history.getCouponId() : null;
    }

    private Map<Long, SmsCoupon> loadCouponsByIds(Collection<Long> couponIds) {
        if (CollectionUtils.isEmpty(couponIds)) {
            return Map.of();
        }
        return smsCouponMapper.selectBatchIds(couponIds).stream()
                .filter(coupon -> coupon.getId() != null)
                .collect(Collectors.toMap(SmsCoupon::getId, coupon -> coupon, (left, right) -> left));
    }

    private SmsCouponHistory requireUnusedHistory(Long memberId, Long couponHistoryId) {
        SmsCouponHistory history = smsCouponHistoryMapper.selectById(couponHistoryId);
        if (history == null || !memberId.equals(history.getMemberId())) {
            throw new BizException("优惠券不存在");
        }
        if (!Integer.valueOf(HISTORY_UNUSED).equals(history.getUseType())) {
            throw new BizException("优惠券已使用或已过期");
        }
        if (history.getOrderId() != null && history.getOrderId() > 0) {
            throw new BizException("优惠券已被其他订单占用");
        }
        return history;
    }

    private boolean isCouponActive(SmsCoupon coupon, LocalDateTime now) {
        if (coupon.getStartTime() != null && now.isBefore(coupon.getStartTime())) {
            return false;
        }
        if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
            return false;
        }
        return true;
    }

    private PortalCheckoutCouponVo toCheckoutCouponVo(SmsCouponHistory history, SmsCoupon coupon) {
        PortalCheckoutCouponVo vo = new PortalCheckoutCouponVo();
        vo.setHistoryId(history.getId());
        vo.setCouponId(coupon.getId());
        vo.setCouponName(coupon.getCouponName());
        vo.setAmount(coupon.getAmount());
        vo.setMinPoint(coupon.getMinPoint());
        vo.setUseType(coupon.getUseType());
        vo.setStartTime(coupon.getStartTime());
        vo.setEndTime(coupon.getEndTime());
        return vo;
    }

    private String buildInactiveReason(SmsCoupon coupon, LocalDateTime now) {
        if (coupon.getStartTime() != null && now.isBefore(coupon.getStartTime())) {
            return "未到使用时间（" + coupon.getStartTime().format(COUPON_TIME_FMT) + " 起）";
        }
        if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
            return "已过使用有效期";
        }
        return "当前不可使用";
    }

    private boolean matchesScope(SmsCoupon coupon, OrderScope scope) {
        if (scope.lines().isEmpty()) {
            return false;
        }
        Integer useType = coupon.getUseType();
        BigDecimal minPoint = coupon.getMinPoint();
        if (useType == null || useType == COUPON_USE_ALL) {
            BigDecimal total = scope.total();
            return minPoint == null || total.compareTo(minPoint) >= 0;
        }
        if (useType == COUPON_USE_SPU) {
            Set<Long> allowed = loadAllowedSpuIds(coupon.getId());
            if (allowed.isEmpty()) {
                return false;
            }
            BigDecimal eligible = sumEligible(scope, line -> line.spuId() != null && allowed.contains(line.spuId()));
            if (eligible.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }
            return minPoint == null || eligible.compareTo(minPoint) >= 0;
        }
        if (useType == COUPON_USE_CATEGORY) {
            Set<Long> allowed = loadAllowedCategoryIds(coupon.getId());
            if (allowed.isEmpty()) {
                return false;
            }
            BigDecimal eligible = sumEligible(
                    scope, line -> line.catalogId() != null && allowed.contains(line.catalogId()));
            if (eligible.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }
            return minPoint == null || eligible.compareTo(minPoint) >= 0;
        }
        return true;
    }

    private BigDecimal sumEligible(OrderScope scope, java.util.function.Predicate<OrderLine> predicate) {
        return scope.lines().stream()
                .filter(predicate)
                .map(OrderLine::lineAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Set<Long> loadAllowedSpuIds(Long couponId) {
        List<SmsCouponSpuRelation> relations = smsCouponSpuRelationMapper.selectList(
                Wrappers.<SmsCouponSpuRelation>lambdaQuery().eq(SmsCouponSpuRelation::getCouponId, couponId));
        return relations.stream()
                .map(SmsCouponSpuRelation::getSpuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<Long> loadAllowedCategoryIds(Long couponId) {
        List<SmsCouponSpuCategoryRelation> relations = smsCouponSpuCategoryRelationMapper.selectList(
                Wrappers.<SmsCouponSpuCategoryRelation>lambdaQuery()
                        .eq(SmsCouponSpuCategoryRelation::getCouponId, couponId));
        return relations.stream()
                .map(SmsCouponSpuCategoryRelation::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private OrderScope resolveOrderScope(Long memberId, PortalCouponTrialBo trialBo) {
        List<PortalOrderItemBo> items = resolveItems(memberId, trialBo);
        if (CollectionUtils.isEmpty(items)) {
            return new OrderScope(List.of());
        }
        List<Long> skuIds = items.stream().map(PortalOrderItemBo::getSkuId).distinct().toList();
        List<PmsSkuInfo> skus = pmsSkuInfoMapper.selectBatchIds(skuIds);
        Map<Long, PmsSkuInfo> skuMap = skus.stream()
                .filter(s -> s.getSkuId() != null)
                .collect(Collectors.toMap(PmsSkuInfo::getSkuId, s -> s, (a, b) -> a));
        Map<Long, BigDecimal> priceMap = portalOrderPriceService.resolveUnitPriceMap(new HashMap<>(skuMap));
        Set<Long> spuIdSet = skus.stream()
                .map(PmsSkuInfo::getSpuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, PmsSpuInfo> spuMap = spuIdSet.isEmpty()
                ? Map.of()
                : pmsSpuInfoMapper.selectBatchIds(spuIdSet).stream()
                        .filter(s -> s.getId() != null)
                        .collect(Collectors.toMap(PmsSpuInfo::getId, s -> s, (a, b) -> a));

        List<OrderLine> lines = new ArrayList<>();
        for (PortalOrderItemBo item : items) {
            PmsSkuInfo sku = skuMap.get(item.getSkuId());
            if (sku == null) {
                continue;
            }
            int qty = item.getQuantity() == null ? 0 : item.getQuantity();
            if (qty <= 0) {
                continue;
            }
            BigDecimal unit = priceMap.getOrDefault(item.getSkuId(), sku.getPrice());
            if (unit == null) {
                continue;
            }
            Long catalogId = null;
            if (sku.getSpuId() != null) {
                PmsSpuInfo spu = spuMap.get(sku.getSpuId());
                if (spu != null) {
                    catalogId = spu.getCatalogId();
                }
            }
            lines.add(new OrderLine(sku.getSpuId(), catalogId, unit.multiply(BigDecimal.valueOf(qty))));
        }
        return new OrderScope(lines);
    }

    private List<PortalOrderItemBo> resolveItems(Long memberId, PortalCouponTrialBo trialBo) {
        if (Boolean.TRUE.equals(trialBo.getUseCart())) {
            PortalCartVo cart = portalCartService.listCart(memberId);
            if (cart.getItems() == null) {
                return List.of();
            }
            return cart.getItems().stream()
                    .filter(i -> Boolean.TRUE.equals(i.getChecked()) && Boolean.TRUE.equals(i.getValid()))
                    .map(i -> {
                        PortalOrderItemBo bo = new PortalOrderItemBo();
                        bo.setSkuId(i.getSkuId());
                        bo.setQuantity(i.getQuantity());
                        return bo;
                    })
                    .collect(Collectors.toList());
        }
        return trialBo.getItems() == null ? List.of() : trialBo.getItems();
    }

    private boolean isInEnableWindow(SmsCoupon coupon, LocalDateTime now) {
        if (coupon.getEnableStartTime() != null && now.isBefore(coupon.getEnableStartTime())) {
            return false;
        }
        if (coupon.getEnableEndTime() != null && now.isAfter(coupon.getEnableEndTime())) {
            return false;
        }
        return true;
    }

    private boolean hasStock(SmsCoupon coupon) {
        if (coupon.getPublishCount() == null || coupon.getPublishCount() <= 0) {
            return true;
        }
        int received = coupon.getReceiveCount() == null ? 0 : coupon.getReceiveCount();
        return received < coupon.getPublishCount();
    }

    private int countMemberReceived(Long memberId, Long couponId) {
        return Math.toIntExact(smsCouponHistoryMapper.selectCount(
                Wrappers.<SmsCouponHistory>lambdaQuery()
                        .eq(SmsCouponHistory::getMemberId, memberId)
                        .eq(SmsCouponHistory::getCouponId, couponId)));
    }

    private boolean canMemberReceive(SmsCoupon coupon, int receivedByMember) {
        if (coupon.getPerLimit() != null && coupon.getPerLimit() > 0 && receivedByMember >= coupon.getPerLimit()) {
            return false;
        }
        return hasStock(coupon);
    }

    private int resolveHistoryStatus(SmsCouponHistory history, SmsCoupon coupon, LocalDateTime now) {
        if (Integer.valueOf(HISTORY_USED).equals(history.getUseType())) {
            return HISTORY_USED;
        }
        if (Integer.valueOf(HISTORY_EXPIRED).equals(history.getUseType())) {
            return HISTORY_EXPIRED;
        }
        if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
            return HISTORY_EXPIRED;
        }
        return HISTORY_UNUSED;
    }

    private record OrderLine(Long spuId, Long catalogId, BigDecimal lineAmount) {}

    private record OrderScope(List<OrderLine> lines) {

        BigDecimal total() {
            return lines.stream().map(OrderLine::lineAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }
}
