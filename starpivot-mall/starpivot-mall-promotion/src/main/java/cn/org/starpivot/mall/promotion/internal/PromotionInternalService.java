package cn.org.starpivot.mall.promotion.internal;

import cn.org.starpivot.api.mall.order.dto.OrderRewardContextDto;
import cn.org.starpivot.api.mall.promotion.dto.*;
import cn.org.starpivot.api.product.dto.SkuDto;
import cn.org.starpivot.mall.common.ProductFeignSupport;
import cn.org.starpivot.mall.portal.domain.bo.PortalCouponTrialBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderItemBo;
import cn.org.starpivot.mall.portal.service.PortalCouponService;
import cn.org.starpivot.mall.portal.service.PortalOrderPriceService;
import cn.org.starpivot.mall.portal.service.PortalSeckillStockService;
import cn.org.starpivot.mall.sms.entity.*;
import cn.org.starpivot.mall.sms.mapper.*;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 营销域内部接口业务（Feign /internal 目标）。
 */
@Service
@RequiredArgsConstructor
public class PromotionInternalService {

    private static final int COUPON_UNUSED = 0;

    private final SmsMemberPriceMapper smsMemberPriceMapper;
    private final SmsSkuFullReductionMapper smsSkuFullReductionMapper;
    private final SmsSkuLadderMapper smsSkuLadderMapper;
    private final SmsSpuBoundsMapper smsSpuBoundsMapper;
    private final SmsCouponHistoryMapper smsCouponHistoryMapper;
    private final SmsHomeSubjectMapper smsHomeSubjectMapper;
    private final PortalCouponService portalCouponService;
    private final PortalOrderPriceService portalOrderPriceService;
    private final PortalSeckillStockService portalSeckillStockService;
    private final ProductFeignSupport productFeignSupport;

    @Transactional(readOnly = true)
    public SkuPriceRulesDto loadSkuPriceRules(SkuPriceRulesRequest request) {
        List<Long> skuIds = request.getSkuIds() == null ? List.of() : request.getSkuIds();
        SkuPriceRulesDto dto = new SkuPriceRulesDto();
        if (skuIds.isEmpty()) {
            dto.setMemberPrices(Map.of());
            dto.setLadders(Map.of());
            dto.setFullReductions(Map.of());
            dto.setSeckillPrices(Map.of());
            return dto;
        }

        Map<Long, BigDecimal> memberPrices = Map.of();
        if (Boolean.TRUE.equals(request.getIncludeMemberPrice()) && request.getMemberLevelId() != null) {
            memberPrices = smsMemberPriceMapper.selectList(
                            Wrappers.<SmsMemberPrice>lambdaQuery()
                                    .in(SmsMemberPrice::getSkuId, skuIds)
                                    .eq(SmsMemberPrice::getMemberLevelId, request.getMemberLevelId()))
                    .stream()
                    .filter(mp -> mp.getMemberPrice() != null)
                    .collect(Collectors.toMap(
                            SmsMemberPrice::getSkuId,
                            SmsMemberPrice::getMemberPrice,
                            (a, b) -> a));
        }

        Map<Long, List<SkuLadderLineDto>> ladders = smsSkuLadderMapper.selectList(
                        Wrappers.<SmsSkuLadder>lambdaQuery().in(SmsSkuLadder::getSkuId, skuIds))
                .stream()
                .collect(Collectors.groupingBy(
                        SmsSkuLadder::getSkuId,
                        Collectors.mapping(this::toLadderDto, Collectors.toList())));

        Map<Long, List<SkuFullReductionLineDto>> fullReductions = smsSkuFullReductionMapper.selectList(
                        Wrappers.<SmsSkuFullReduction>lambdaQuery().in(SmsSkuFullReduction::getSkuId, skuIds))
                .stream()
                .collect(Collectors.groupingBy(
                        SmsSkuFullReduction::getSkuId,
                        Collectors.mapping(this::toFullReductionDto, Collectors.toList())));

        Map<Long, SkuDto> skuMap = productFeignSupport.requireSkuMap(skuIds);
        Map<Long, BigDecimal> seckillPrices = portalOrderPriceService.resolveUnitPriceMap(skuMap).entrySet().stream()
                .filter(e -> e.getValue() != null)
                .filter(e -> {
                    SkuDto sku = skuMap.get(e.getKey());
                    return sku != null && sku.getPrice() != null && e.getValue().compareTo(sku.getPrice()) < 0;
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));

        dto.setMemberPrices(memberPrices);
        dto.setLadders(ladders);
        dto.setFullReductions(fullReductions);
        dto.setSeckillPrices(seckillPrices);
        return dto;
    }

    @Transactional(readOnly = true)
    public CouponCalculateResultDto calculateCoupon(CouponCalculateRequest request) {
        PortalCouponTrialBo trialBo = new PortalCouponTrialBo();
        trialBo.setUseCart(false);
        if (!CollectionUtils.isEmpty(request.getItems())) {
            trialBo.setItems(request.getItems().stream().map(this::toOrderItemBo).collect(Collectors.toList()));
        }
        BigDecimal discount = portalCouponService.calculateDiscount(
                request.getMemberId(), request.getCouponHistoryId(), trialBo);
        CouponCalculateResultDto result = new CouponCalculateResultDto();
        result.setDiscountAmount(discount == null ? BigDecimal.ZERO : discount);
        result.setCouponId(portalCouponService.resolveCouponId(request.getCouponHistoryId()));
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void consumeCoupon(CouponConsumeRequest request) {
        portalCouponService.lockToOrder(
                request.getCouponHistoryId(), request.getMemberId(), request.getOrderId(), request.getOrderSn());
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmCouponByOrder(Long orderId) {
        portalCouponService.confirmUsed(orderId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void releaseCouponByOrder(Long orderId) {
        portalCouponService.releaseByOrder(orderId);
    }

    @Transactional(readOnly = true)
    public Map<Long, OrderRewardContextDto.SpuBoundsLine> batchSpuBounds(Collection<Long> spuIds) {
        if (CollectionUtils.isEmpty(spuIds)) {
            return Map.of();
        }
        return smsSpuBoundsMapper.selectList(
                        Wrappers.<SmsSpuBounds>lambdaQuery().in(SmsSpuBounds::getSpuId, spuIds))
                .stream()
                .collect(Collectors.toMap(
                        SmsSpuBounds::getSpuId,
                        this::toBoundsLine,
                        (a, b) -> a));
    }

    @Transactional(rollbackFor = Exception.class)
    public void upsertSpuBounds(SpuBoundsUpsertRequest request) {
        SmsSpuBounds existing = smsSpuBoundsMapper.selectOne(
                Wrappers.<SmsSpuBounds>lambdaQuery().eq(SmsSpuBounds::getSpuId, request.getSpuId()));
        if (existing == null) {
            SmsSpuBounds entity = new SmsSpuBounds();
            entity.setSpuId(request.getSpuId());
            entity.setBuyBounds(normalizeBounds(request.getBuyBounds()));
            entity.setGrowBounds(normalizeBounds(request.getGrowBounds()));
            entity.setWork(1);
            smsSpuBoundsMapper.insert(entity);
        } else {
            existing.setBuyBounds(normalizeBounds(request.getBuyBounds()));
            existing.setGrowBounds(normalizeBounds(request.getGrowBounds()));
            if (existing.getWork() == null) {
                existing.setWork(1);
            }
            smsSpuBoundsMapper.updateById(existing);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean reserveSeckill(SeckillReserveRequest request) {
        boolean ok = portalSeckillStockService.reserve(
                request.getPromotionId(),
                request.getSessionId(),
                request.getSkuId(),
                request.getMemberId(),
                request.getQuantity() == null ? 0 : request.getQuantity(),
                request.getSeckillLimit() == null ? 0 : request.getSeckillLimit());
        if (ok && request.getOrderSn() != null) {
            portalSeckillStockService.bindOrder(
                    request.getOrderSn(),
                    request.getPromotionId(),
                    request.getSessionId(),
                    request.getSkuId(),
                    request.getMemberId(),
                    request.getQuantity() == null ? 0 : request.getQuantity(),
                    request.getSeckillLimit() == null ? 0 : request.getSeckillLimit());
        }
        return ok;
    }

    @Transactional(rollbackFor = Exception.class)
    public void releaseSeckill(SeckillReleaseRequest request) {
        portalSeckillStockService.release(
                request.getPromotionId(),
                request.getSessionId(),
                request.getSkuId(),
                request.getMemberId(),
                request.getQuantity() == null ? 0 : request.getQuantity(),
                request.getSeckillLimit() == null ? 0 : request.getSeckillLimit());
    }

    @Transactional(rollbackFor = Exception.class)
    public void releaseSeckillByOrderSn(String orderSn) {
        portalSeckillStockService.releaseByOrderSn(orderSn);
    }

    @Transactional(readOnly = true)
    public int countUnusedCoupons(Long memberId) {
        if (memberId == null) {
            return 0;
        }
        Long count = smsCouponHistoryMapper.selectCount(
                Wrappers.<SmsCouponHistory>lambdaQuery()
                        .eq(SmsCouponHistory::getMemberId, memberId)
                        .eq(SmsCouponHistory::getUseType, COUPON_UNUSED));
        return count != null ? count.intValue() : 0;
    }

    @Transactional(readOnly = true)
    public HomeSubjectDto getSubject(Long subjectId) {
        SmsHomeSubject subject = smsHomeSubjectMapper.selectById(subjectId);
        if (subject == null) {
            return null;
        }
        HomeSubjectDto dto = new HomeSubjectDto();
        dto.setId(subject.getId());
        dto.setName(subject.getName());
        dto.setTitle(subject.getTitle());
        dto.setStatus(subject.getStatus());
        dto.setUrl(subject.getUrl());
        dto.setImg(subject.getImg());
        return dto;
    }

    private SkuLadderLineDto toLadderDto(SmsSkuLadder ladder) {
        SkuLadderLineDto dto = new SkuLadderLineDto();
        dto.setFullCount(ladder.getFullCount());
        dto.setDiscount(ladder.getDiscount());
        dto.setPrice(ladder.getPrice());
        return dto;
    }

    private SkuFullReductionLineDto toFullReductionDto(SmsSkuFullReduction fr) {
        SkuFullReductionLineDto dto = new SkuFullReductionLineDto();
        dto.setFullPrice(fr.getFullPrice());
        dto.setReducePrice(fr.getReducePrice());
        return dto;
    }

    private OrderRewardContextDto.SpuBoundsLine toBoundsLine(SmsSpuBounds bounds) {
        OrderRewardContextDto.SpuBoundsLine line = new OrderRewardContextDto.SpuBoundsLine();
        line.setSpuId(bounds.getSpuId());
        line.setBuyBounds(bounds.getBuyBounds());
        line.setGrowBounds(bounds.getGrowBounds());
        line.setWork(bounds.getWork());
        return line;
    }

    private BigDecimal normalizeBounds(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private PortalOrderItemBo toOrderItemBo(CouponTrialItemDto item) {
        PortalOrderItemBo bo = new PortalOrderItemBo();
        bo.setSkuId(item.getSkuId());
        bo.setQuantity(item.getQuantity());
        return bo;
    }
}
