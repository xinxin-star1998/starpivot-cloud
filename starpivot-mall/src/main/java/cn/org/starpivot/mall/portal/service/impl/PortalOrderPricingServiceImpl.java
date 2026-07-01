package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.config.MallOrderProperties;
import cn.org.starpivot.mall.portal.domain.bo.PortalCouponTrialBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderItemBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderPriceTrialBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderPriceLineVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderPriceTrialVo;
import cn.org.starpivot.mall.portal.service.PortalCouponService;
import cn.org.starpivot.mall.portal.service.PortalOrderPriceService;
import cn.org.starpivot.mall.portal.service.PortalOrderPricingResult;
import cn.org.starpivot.mall.portal.service.PortalOrderPricingService;
import cn.org.starpivot.mall.pms.entity.PmsSkuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsSkuInfoMapper;
import cn.org.starpivot.mall.sms.entity.SmsMemberPrice;
import cn.org.starpivot.mall.sms.entity.SmsSkuFullReduction;
import cn.org.starpivot.mall.sms.entity.SmsSkuLadder;
import cn.org.starpivot.mall.sms.mapper.SmsMemberPriceMapper;
import cn.org.starpivot.mall.sms.mapper.SmsSkuFullReductionMapper;
import cn.org.starpivot.mall.sms.mapper.SmsSkuLadderMapper;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import cn.org.starpivot.mall.ums.entity.UmsMemberLevel;
import cn.org.starpivot.mall.ums.mapper.UmsMemberLevelMapper;
import cn.org.starpivot.mall.ums.mapper.UmsMemberMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortalOrderPricingServiceImpl implements PortalOrderPricingService {

    private static final int MONEY_SCALE = 2;

    private final UmsMemberMapper umsMemberMapper;
    private final UmsMemberLevelMapper umsMemberLevelMapper;
    private final PmsSkuInfoMapper pmsSkuInfoMapper;
    private final PortalOrderPriceService portalOrderPriceService;
    private final PortalCouponService portalCouponService;
    private final SmsMemberPriceMapper smsMemberPriceMapper;
    private final SmsSkuFullReductionMapper smsSkuFullReductionMapper;
    private final SmsSkuLadderMapper smsSkuLadderMapper;
    private final MallOrderProperties mallOrderProperties;

    @Override
    @Transactional(readOnly = true)
    public PortalOrderPriceTrialVo trial(Long memberId, PortalOrderPriceTrialBo bo) {
        List<PortalOrderItemBo> items = bo.getItems() == null ? List.of() : bo.getItems();
        PortalOrderPricingResult result = calculate(
                memberId,
                items,
                bo.getCouponHistoryId(),
                bo.getUseIntegration(),
                false);
        return toTrialVo(memberId, result);
    }

    @Override
    @Transactional(readOnly = true)
    public PortalOrderPricingResult calculate(
            Long memberId,
            List<PortalOrderItemBo> orderItems,
            Long couponHistoryId,
            Integer useIntegration,
            boolean validateIntegration) {
        if (CollectionUtils.isEmpty(orderItems)) {
            throw new BizException("下单商品不能为空");
        }

        UmsMember member = requireMember(memberId);
        UmsMemberLevel level = resolveMemberLevel(member.getLevelId());

        List<Long> skuIds = orderItems.stream().map(PortalOrderItemBo::getSkuId).distinct().toList();
        Map<Long, PmsSkuInfo> skuMap = loadSkuMap(skuIds);
        Map<Long, BigDecimal> seckillPriceMap = portalOrderPriceService.resolveUnitPriceMap(skuMap);
        Map<Long, SmsMemberPrice> memberPriceMap = loadMemberPriceMap(skuIds, level);
        Map<Long, List<SmsSkuFullReduction>> fullReductionMap = loadFullReductionMap(skuIds);
        Map<Long, List<SmsSkuLadder>> ladderMap = loadLadderMap(skuIds);

        List<PortalOrderPricingResult.PricedLine> lines = new ArrayList<>();
        BigDecimal originalAmount = BigDecimal.ZERO;
        BigDecimal promotionAmount = BigDecimal.ZERO;

        for (PortalOrderItemBo itemBo : orderItems) {
            PmsSkuInfo sku = skuMap.get(itemBo.getSkuId());
            if (sku == null || sku.getPrice() == null) {
                throw new BizException("SKU不存在或价格异常：" + itemBo.getSkuId());
            }
            int qty = itemBo.getQuantity() == null || itemBo.getQuantity() <= 0 ? 1 : itemBo.getQuantity();
            BigDecimal originalUnit = money(sku.getPrice());
            BigDecimal unitPrice = resolveUnitPrice(
                    sku, seckillPriceMap.get(itemBo.getSkuId()), memberPriceMap.get(itemBo.getSkuId()), level);

            BigDecimal lineOriginal = money(originalUnit.multiply(BigDecimal.valueOf(qty)));
            BigDecimal lineAfterUnitPromo = money(unitPrice.multiply(BigDecimal.valueOf(qty)));
            BigDecimal lineAmount = applyLadderAndFullReduction(
                    itemBo.getSkuId(), qty, unitPrice, lineAfterUnitPromo, ladderMap, fullReductionMap);

            BigDecimal linePromotion = money(lineOriginal.subtract(lineAmount).max(BigDecimal.ZERO));

            PortalOrderPricingResult.PricedLine line = new PortalOrderPricingResult.PricedLine();
            line.setSkuId(itemBo.getSkuId());
            line.setQuantity(qty);
            line.setOriginalUnitPrice(originalUnit);
            line.setUnitPrice(unitPrice);
            line.setLineOriginalAmount(lineOriginal);
            line.setPromotionAmount(linePromotion);
            line.setLineAmount(lineAmount);
            lines.add(line);

            originalAmount = money(originalAmount.add(lineOriginal));
            promotionAmount = money(promotionAmount.add(linePromotion));
        }

        BigDecimal merchandiseAmount = money(originalAmount.subtract(promotionAmount).max(BigDecimal.ZERO));

        PortalCouponTrialBo couponTrial = new PortalCouponTrialBo();
        couponTrial.setUseCart(false);
        couponTrial.setItems(orderItems);
        BigDecimal couponAmount = portalCouponService.calculateDiscount(memberId, couponHistoryId, couponTrial);
        Long couponId = portalCouponService.resolveCouponId(couponHistoryId);
        couponAmount = money(couponAmount == null ? BigDecimal.ZERO : couponAmount);

        BigDecimal afterCoupon = money(merchandiseAmount.subtract(couponAmount).max(BigDecimal.ZERO));

        int availableIntegration = member.getIntegration() == null ? 0 : member.getIntegration();
        int maxUsableIntegration = calcMaxUsableIntegration(afterCoupon, availableIntegration);
        int actualUseIntegration = resolveUseIntegration(useIntegration, maxUsableIntegration);
        if (validateIntegration && actualUseIntegration > availableIntegration) {
            throw new BizException("积分不足，当前可用 " + availableIntegration);
        }
        BigDecimal integrationAmount = integrationToMoney(actualUseIntegration);

        BigDecimal afterIntegration = money(afterCoupon.subtract(integrationAmount).max(BigDecimal.ZERO));
        boolean freeFreight = isFreeFreight(level, merchandiseAmount);
        BigDecimal freight = freeFreight ? BigDecimal.ZERO : money(mallOrderProperties.getDefaultFreight());
        BigDecimal payAmount = money(afterIntegration.add(freight));

        PortalOrderPricingResult result = new PortalOrderPricingResult();
        result.setOriginalAmount(originalAmount);
        result.setPromotionAmount(promotionAmount);
        result.setMerchandiseAmount(merchandiseAmount);
        result.setCouponAmount(couponAmount);
        result.setCouponId(couponId);
        result.setIntegrationAmount(integrationAmount);
        result.setUseIntegration(actualUseIntegration);
        result.setFreightAmount(freight);
        result.setFreeFreight(freeFreight);
        result.setPayAmount(payAmount);
        result.setLines(lines);
        return result;
    }

    private PortalOrderPriceTrialVo toTrialVo(Long memberId, PortalOrderPricingResult result) {
        UmsMember member = requireMember(memberId);
        int available = member.getIntegration() == null ? 0 : member.getIntegration();
        BigDecimal afterCoupon = money(result.getMerchandiseAmount().subtract(result.getCouponAmount()).max(BigDecimal.ZERO));
        int maxUsable = calcMaxUsableIntegration(afterCoupon, available);

        PortalOrderPriceTrialVo vo = new PortalOrderPriceTrialVo();
        vo.setOriginalAmount(result.getOriginalAmount());
        vo.setPromotionAmount(result.getPromotionAmount());
        vo.setMerchandiseAmount(result.getMerchandiseAmount());
        vo.setCouponAmount(result.getCouponAmount());
        vo.setIntegrationAmount(result.getIntegrationAmount());
        vo.setUseIntegration(result.getUseIntegration());
        vo.setAvailableIntegration(available);
        vo.setMaxUsableIntegration(maxUsable);
        vo.setFreightAmount(result.getFreightAmount());
        vo.setFreeFreight(result.isFreeFreight());
        vo.setPayAmount(result.getPayAmount());
        vo.setLines(result.getLines().stream().map(this::toLineVo).collect(Collectors.toList()));
        return vo;
    }

    private PortalOrderPriceLineVo toLineVo(PortalOrderPricingResult.PricedLine line) {
        PortalOrderPriceLineVo vo = new PortalOrderPriceLineVo();
        vo.setSkuId(line.getSkuId());
        vo.setQuantity(line.getQuantity());
        vo.setOriginalUnitPrice(line.getOriginalUnitPrice());
        vo.setUnitPrice(line.getUnitPrice());
        vo.setLineOriginalAmount(line.getLineOriginalAmount());
        vo.setPromotionAmount(line.getPromotionAmount());
        vo.setLineAmount(line.getLineAmount());
        return vo;
    }

    private UmsMember requireMember(Long memberId) {
        UmsMember member = umsMemberMapper.selectById(memberId);
        if (member == null) {
            throw new BizException("会员不存在");
        }
        return member;
    }

    private UmsMemberLevel resolveMemberLevel(Long levelId) {
        if (levelId == null) {
            return null;
        }
        return umsMemberLevelMapper.selectById(levelId);
    }

    private Map<Long, PmsSkuInfo> loadSkuMap(List<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Map.of();
        }
        return pmsSkuInfoMapper.selectBatchIds(skuIds).stream()
                .filter(sku -> sku.getSkuId() != null)
                .collect(Collectors.toMap(PmsSkuInfo::getSkuId, sku -> sku, (a, b) -> a));
    }

    private Map<Long, SmsMemberPrice> loadMemberPriceMap(List<Long> skuIds, UmsMemberLevel level) {
        if (level == null || !Integer.valueOf(1).equals(level.getPrivilegeMemberPrice()) || CollectionUtils.isEmpty(skuIds)) {
            return Map.of();
        }
        List<SmsMemberPrice> list = smsMemberPriceMapper.selectList(
                Wrappers.<SmsMemberPrice>lambdaQuery()
                        .in(SmsMemberPrice::getSkuId, skuIds)
                        .eq(SmsMemberPrice::getMemberLevelId, level.getId()));
        Map<Long, SmsMemberPrice> map = new HashMap<>();
        for (SmsMemberPrice mp : list) {
            map.putIfAbsent(mp.getSkuId(), mp);
        }
        return map;
    }

    private Map<Long, List<SmsSkuFullReduction>> loadFullReductionMap(List<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Map.of();
        }
        return smsSkuFullReductionMapper.selectList(
                        Wrappers.<SmsSkuFullReduction>lambdaQuery().in(SmsSkuFullReduction::getSkuId, skuIds))
                .stream()
                .collect(Collectors.groupingBy(SmsSkuFullReduction::getSkuId));
    }

    private Map<Long, List<SmsSkuLadder>> loadLadderMap(List<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Map.of();
        }
        return smsSkuLadderMapper.selectList(
                        Wrappers.<SmsSkuLadder>lambdaQuery().in(SmsSkuLadder::getSkuId, skuIds))
                .stream()
                .collect(Collectors.groupingBy(SmsSkuLadder::getSkuId));
    }

    /**
     * 单价优先级：秒杀价 &gt; 会员价（更低者） &gt; SKU 原价。
     */
    private BigDecimal resolveUnitPrice(
            PmsSkuInfo sku,
            BigDecimal seckillPrice,
            SmsMemberPrice memberPrice,
            UmsMemberLevel level) {
        BigDecimal base = money(sku.getPrice());
        if (seckillPrice != null && seckillPrice.compareTo(BigDecimal.ZERO) > 0) {
            return money(seckillPrice);
        }
        if (level != null
                && Integer.valueOf(1).equals(level.getPrivilegeMemberPrice())
                && memberPrice != null
                && memberPrice.getMemberPrice() != null
                && memberPrice.getMemberPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal mp = money(memberPrice.getMemberPrice());
            return mp.compareTo(base) < 0 ? mp : base;
        }
        return base;
    }

    private BigDecimal applyLadderAndFullReduction(
            Long skuId,
            int qty,
            BigDecimal unitPrice,
            BigDecimal lineAfterUnitPromo,
            Map<Long, List<SmsSkuLadder>> ladderMap,
            Map<Long, List<SmsSkuFullReduction>> fullReductionMap) {
        BigDecimal lineAmount = lineAfterUnitPromo;

        List<SmsSkuLadder> ladders = ladderMap.getOrDefault(skuId, List.of());
        SmsSkuLadder bestLadder = ladders.stream()
                .filter(l -> l.getFullCount() != null && qty >= l.getFullCount())
                .max(Comparator.comparing(SmsSkuLadder::getFullCount))
                .orElse(null);
        if (bestLadder != null) {
            if (bestLadder.getPrice() != null && bestLadder.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                lineAmount = money(bestLadder.getPrice().multiply(BigDecimal.valueOf(qty)));
            } else if (bestLadder.getDiscount() != null && bestLadder.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                lineAmount = money(unitPrice.multiply(BigDecimal.valueOf(qty)).multiply(bestLadder.getDiscount()));
            }
        }

        List<SmsSkuFullReduction> reductions = fullReductionMap.getOrDefault(skuId, List.of());
        BigDecimal bestReduce = BigDecimal.ZERO;
        for (SmsSkuFullReduction fr : reductions) {
            if (fr.getFullPrice() == null || fr.getReducePrice() == null) {
                continue;
            }
            if (lineAmount.compareTo(fr.getFullPrice()) >= 0 && fr.getReducePrice().compareTo(bestReduce) > 0) {
                bestReduce = fr.getReducePrice();
            }
        }
        if (bestReduce.compareTo(BigDecimal.ZERO) > 0) {
            lineAmount = money(lineAmount.subtract(bestReduce).max(BigDecimal.ZERO));
        }
        return lineAmount;
    }

    private boolean isFreeFreight(UmsMemberLevel level, BigDecimal merchandiseAmount) {
        if (level == null) {
            return false;
        }
        if (Integer.valueOf(1).equals(level.getPrivilegeFreeFreight())) {
            return true;
        }
        if (level.getFreeFreightPoint() != null
                && merchandiseAmount.compareTo(level.getFreeFreightPoint()) >= 0) {
            return true;
        }
        return false;
    }

    private int calcMaxUsableIntegration(BigDecimal afterCouponAmount, int availableIntegration) {
        if (availableIntegration <= 0 || afterCouponAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        BigDecimal rate = mallOrderProperties.getIntegrationYuanRate();
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        BigDecimal maxRate = mallOrderProperties.getIntegrationMaxRate();
        if (maxRate == null || maxRate.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        BigDecimal maxMoney = money(afterCouponAmount.multiply(maxRate));
        int maxByMoney = maxMoney.divide(rate, 0, RoundingMode.DOWN).intValue();
        return Math.min(availableIntegration, Math.max(maxByMoney, 0));
    }

    private int resolveUseIntegration(Integer requested, int maxUsable) {
        if (requested == null || requested <= 0 || maxUsable <= 0) {
            return 0;
        }
        return Math.min(requested, maxUsable);
    }

    private BigDecimal integrationToMoney(int points) {
        if (points <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = mallOrderProperties.getIntegrationYuanRate();
        return money(rate.multiply(BigDecimal.valueOf(points)));
    }

    private BigDecimal money(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
