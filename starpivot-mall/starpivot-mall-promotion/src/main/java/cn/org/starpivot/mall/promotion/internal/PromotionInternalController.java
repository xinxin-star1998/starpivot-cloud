package cn.org.starpivot.mall.promotion.internal;

import cn.org.starpivot.api.mall.order.dto.OrderRewardContextDto;
import cn.org.starpivot.api.mall.promotion.dto.*;
import cn.org.starpivot.api.product.dto.SkuDto;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.common.ProductFeignSupport;
import cn.org.starpivot.mall.portal.domain.bo.PortalCouponTrialBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderItemBo;
import cn.org.starpivot.mall.portal.service.PortalCouponService;
import cn.org.starpivot.mall.portal.service.PortalOrderPriceService;
import cn.org.starpivot.mall.portal.service.PortalSeckillStockService;
import cn.org.starpivot.mall.sms.entity.*;
import cn.org.starpivot.mall.sms.mapper.*;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Hidden
@RestController
@RequestMapping("/internal/mall/promotion")
@RequiredArgsConstructor
public class PromotionInternalController {

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

    @PostMapping("/sku-price-rules")
    public Result<SkuPriceRulesDto> loadSkuPriceRules(@RequestBody SkuPriceRulesRequest request) {
        List<Long> skuIds = request.getSkuIds() == null ? List.of() : request.getSkuIds();
        SkuPriceRulesDto dto = new SkuPriceRulesDto();
        if (skuIds.isEmpty()) {
            dto.setMemberPrices(Map.of());
            dto.setLadders(Map.of());
            dto.setFullReductions(Map.of());
            dto.setSeckillPrices(Map.of());
            return Result.success(dto);
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
        return Result.success(dto);
    }

    @PostMapping("/coupon/calculate")
    public Result<CouponCalculateResultDto> calculateCoupon(@RequestBody CouponCalculateRequest request) {
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
        return Result.success(result);
    }

    @PostMapping("/coupon/consume")
    public Result<Void> consumeCoupon(@RequestBody CouponConsumeRequest request) {
        portalCouponService.lockToOrder(
                request.getCouponHistoryId(), request.getMemberId(), request.getOrderId(), request.getOrderSn());
        return Result.success();
    }

    @PostMapping("/coupon/confirm-by-order/{orderId}")
    public Result<Void> confirmCouponByOrder(@PathVariable("orderId") Long orderId) {
        portalCouponService.confirmUsed(orderId);
        return Result.success();
    }

    @PostMapping("/coupon/release-by-order/{orderId}")
    public Result<Void> releaseCouponByOrder(@PathVariable("orderId") Long orderId) {
        portalCouponService.releaseByOrder(orderId);
        return Result.success();
    }

    @PostMapping("/spu-bounds/batch")
    public Result<Map<Long, OrderRewardContextDto.SpuBoundsLine>> batchSpuBounds(@RequestBody Collection<Long> spuIds) {
        if (CollectionUtils.isEmpty(spuIds)) {
            return Result.success(Map.of());
        }
        Map<Long, OrderRewardContextDto.SpuBoundsLine> map = smsSpuBoundsMapper.selectList(
                        Wrappers.<SmsSpuBounds>lambdaQuery().in(SmsSpuBounds::getSpuId, spuIds))
                .stream()
                .collect(Collectors.toMap(
                        SmsSpuBounds::getSpuId,
                        this::toBoundsLine,
                        (a, b) -> a));
        return Result.success(map);
    }

    @PutMapping("/spu-bounds/upsert")
    public Result<Void> upsertSpuBounds(@RequestBody SpuBoundsUpsertRequest request) {
        if (request == null || request.getSpuId() == null) {
            return Result.badRequest("SPU ID不能为空");
        }
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
        return Result.success();
    }

    @PostMapping("/seckill/reserve")
    public Result<Boolean> reserveSeckill(@RequestBody SeckillReserveRequest request) {
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
        return Result.success(ok);
    }

    @PostMapping("/seckill/release")
    public Result<Void> releaseSeckill(@RequestBody SeckillReleaseRequest request) {
        portalSeckillStockService.release(
                request.getPromotionId(),
                request.getSessionId(),
                request.getSkuId(),
                request.getMemberId(),
                request.getQuantity() == null ? 0 : request.getQuantity(),
                request.getSeckillLimit() == null ? 0 : request.getSeckillLimit());
        return Result.success();
    }

    @PostMapping("/seckill/release-by-order-sn")
    public Result<Void> releaseSeckillByOrderSn(@RequestParam("orderSn") String orderSn) {
        portalSeckillStockService.releaseByOrderSn(orderSn);
        return Result.success();
    }

    private static final int COUPON_UNUSED = 0;

    @GetMapping("/coupon/unused-count/{memberId}")
    public Result<Integer> countUnusedCoupons(@PathVariable("memberId") Long memberId) {
        if (memberId == null) {
            return Result.success(0);
        }
        Long count = smsCouponHistoryMapper.selectCount(
                Wrappers.<SmsCouponHistory>lambdaQuery()
                        .eq(SmsCouponHistory::getMemberId, memberId)
                        .eq(SmsCouponHistory::getUseType, COUPON_UNUSED));
        return Result.success(count != null ? count.intValue() : 0);
    }

    @GetMapping("/subject/{subjectId}")
    public Result<HomeSubjectDto> getSubject(@PathVariable("subjectId") Long subjectId) {
        SmsHomeSubject subject = smsHomeSubjectMapper.selectById(subjectId);
        if (subject == null) {
            return Result.notFound("专题不存在");
        }
        HomeSubjectDto dto = new HomeSubjectDto();
        dto.setId(subject.getId());
        dto.setName(subject.getName());
        dto.setTitle(subject.getTitle());
        dto.setStatus(subject.getStatus());
        dto.setUrl(subject.getUrl());
        dto.setImg(subject.getImg());
        return Result.success(dto);
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
