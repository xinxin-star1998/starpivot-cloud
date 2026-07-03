package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.api.mall.promotion.dto.CouponCalculateResultDto;
import cn.org.starpivot.api.mall.promotion.dto.SkuFullReductionLineDto;
import cn.org.starpivot.api.mall.promotion.dto.SkuLadderLineDto;
import cn.org.starpivot.api.mall.promotion.dto.SkuPriceRulesDto;
import cn.org.starpivot.api.member.dto.MemberDto;
import cn.org.starpivot.api.member.dto.MemberLevelDto;
import cn.org.starpivot.api.product.dto.SkuDto;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.common.MemberFeignSupport;
import cn.org.starpivot.mall.common.ProductFeignSupport;
import cn.org.starpivot.mall.common.PromotionFeignSupport;
import cn.org.starpivot.mall.config.MallOrderProperties;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderItemBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderPriceTrialBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderPriceLineVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderPriceTrialVo;
import cn.org.starpivot.mall.portal.service.PortalOrderPricingResult;
import cn.org.starpivot.mall.portal.service.PortalOrderPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortalOrderPricingServiceImpl implements PortalOrderPricingService {

    private static final int MONEY_SCALE = 2;

    private final MemberFeignSupport memberFeignSupport;
    private final ProductFeignSupport productFeignSupport;
    private final PromotionFeignSupport promotionFeignSupport;
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

        MemberDto member = requireMember(memberId);
        MemberLevelDto level = resolveMemberLevel(member.getLevelId());

        List<Long> skuIds = orderItems.stream().map(PortalOrderItemBo::getSkuId).distinct().toList();
        Map<Long, SkuDto> skuMap = loadSkuMap(skuIds);
        SkuPriceRulesDto priceRules = promotionFeignSupport.loadSkuPriceRules(skuIds, level);
        Map<Long, BigDecimal> seckillPriceMap = priceRules.getSeckillPrices() == null ? Map.of() : priceRules.getSeckillPrices();
        Map<Long, BigDecimal> memberPriceMap = priceRules.getMemberPrices() == null ? Map.of() : priceRules.getMemberPrices();
        Map<Long, List<SkuLadderLineDto>> ladderMap = priceRules.getLadders() == null ? Map.of() : priceRules.getLadders();
        Map<Long, List<SkuFullReductionLineDto>> fullReductionMap =
                priceRules.getFullReductions() == null ? Map.of() : priceRules.getFullReductions();

        List<PortalOrderPricingResult.PricedLine> lines = new ArrayList<>();
        BigDecimal originalAmount = BigDecimal.ZERO;
        BigDecimal promotionAmount = BigDecimal.ZERO;

        for (PortalOrderItemBo itemBo : orderItems) {
            SkuDto sku = skuMap.get(itemBo.getSkuId());
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

        CouponCalculateResultDto couponResult =
                promotionFeignSupport.calculateCoupon(memberId, couponHistoryId, orderItems);
        BigDecimal couponAmount = money(couponResult.getDiscountAmount() == null ? BigDecimal.ZERO : couponResult.getDiscountAmount());
        Long couponId = couponResult.getCouponId();

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
        MemberDto member = requireMember(memberId);
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

    private MemberDto requireMember(Long memberId) {
        return memberFeignSupport.requireMember(memberId);
    }

    private MemberLevelDto resolveMemberLevel(Long levelId) {
        return memberFeignSupport.requireMemberLevel(levelId);
    }

    private Map<Long, SkuDto> loadSkuMap(List<Long> skuIds) {
        return productFeignSupport.requireSkuMap(skuIds);
    }

    /**
     * 单价优先级：秒杀价 &gt; 会员价（更低者） &gt; SKU 原价。
     */
    private BigDecimal resolveUnitPrice(
            SkuDto sku,
            BigDecimal seckillPrice,
            BigDecimal memberPrice,
            MemberLevelDto level) {
        BigDecimal base = money(sku.getPrice());
        if (seckillPrice != null && seckillPrice.compareTo(BigDecimal.ZERO) > 0) {
            return money(seckillPrice);
        }
        if (level != null
                && Integer.valueOf(1).equals(level.getPriviledgeMemberPrice())
                && memberPrice != null
                && memberPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal mp = money(memberPrice);
            return mp.compareTo(base) < 0 ? mp : base;
        }
        return base;
    }

    private BigDecimal applyLadderAndFullReduction(
            Long skuId,
            int qty,
            BigDecimal unitPrice,
            BigDecimal lineAfterUnitPromo,
            Map<Long, List<SkuLadderLineDto>> ladderMap,
            Map<Long, List<SkuFullReductionLineDto>> fullReductionMap) {
        BigDecimal lineAmount = lineAfterUnitPromo;

        List<SkuLadderLineDto> ladders = ladderMap.getOrDefault(skuId, List.of());
        SkuLadderLineDto bestLadder = ladders.stream()
                .filter(l -> l.getFullCount() != null && qty >= l.getFullCount())
                .max(Comparator.comparing(SkuLadderLineDto::getFullCount))
                .orElse(null);
        if (bestLadder != null) {
            if (bestLadder.getPrice() != null && bestLadder.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                lineAmount = money(bestLadder.getPrice().multiply(BigDecimal.valueOf(qty)));
            } else if (bestLadder.getDiscount() != null && bestLadder.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                lineAmount = money(unitPrice.multiply(BigDecimal.valueOf(qty)).multiply(bestLadder.getDiscount()));
            }
        }

        List<SkuFullReductionLineDto> reductions = fullReductionMap.getOrDefault(skuId, List.of());
        BigDecimal bestReduce = BigDecimal.ZERO;
        for (SkuFullReductionLineDto fr : reductions) {
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

    private boolean isFreeFreight(MemberLevelDto level, BigDecimal merchandiseAmount) {
        if (level == null) {
            return false;
        }
        if (Integer.valueOf(1).equals(level.getPriviledgeFreeFreight())) {
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
