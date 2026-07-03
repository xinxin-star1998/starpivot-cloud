package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.api.product.dto.SkuDto;
import cn.org.starpivot.mall.portal.service.PortalOrderPriceService;
import cn.org.starpivot.mall.sms.entity.SmsSeckillPromotion;
import cn.org.starpivot.mall.sms.entity.SmsSeckillSkuRelation;
import cn.org.starpivot.mall.sms.mapper.SmsSeckillPromotionMapper;
import cn.org.starpivot.mall.sms.mapper.SmsSeckillSkuRelationMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortalOrderPriceServiceImpl implements PortalOrderPriceService {

    private final SmsSeckillPromotionMapper smsSeckillPromotionMapper;
    private final SmsSeckillSkuRelationMapper smsSeckillSkuRelationMapper;

    @Override
    public BigDecimal resolveUnitPrice(Long skuId, SkuDto sku) {
        if (skuId == null || sku == null || sku.getPrice() == null) {
            return sku != null ? sku.getPrice() : null;
        }
        BigDecimal seckill = findActiveSeckillPrice(skuId);
        return seckill != null ? seckill : sku.getPrice();
    }

    @Override
    public Map<Long, BigDecimal> resolveUnitPriceMap(Map<Long, SkuDto> skuMap) {
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        skuMap.forEach((skuId, sku) -> result.put(skuId, resolveUnitPrice(skuId, sku)));
        return result;
    }

    private BigDecimal findActiveSeckillPrice(Long skuId) {
        SmsSeckillPromotion promotion = findActivePromotion();
        if (promotion == null) {
            return null;
        }
        SmsSeckillSkuRelation relation = smsSeckillSkuRelationMapper.selectOne(
                Wrappers.<SmsSeckillSkuRelation>lambdaQuery()
                        .eq(SmsSeckillSkuRelation::getPromotionId, promotion.getId())
                        .eq(SmsSeckillSkuRelation::getSkuId, skuId)
                        .last("LIMIT 1"));
        return relation != null ? relation.getSeckillPrice() : null;
    }

    private SmsSeckillPromotion findActivePromotion() {
        LocalDateTime now = LocalDateTime.now();
        return smsSeckillPromotionMapper.selectOne(
                Wrappers.<SmsSeckillPromotion>lambdaQuery()
                        .eq(SmsSeckillPromotion::getStatus, 1)
                        .and(w -> w.isNull(SmsSeckillPromotion::getStartTime).or().le(SmsSeckillPromotion::getStartTime, now))
                        .and(w -> w.isNull(SmsSeckillPromotion::getEndTime).or().ge(SmsSeckillPromotion::getEndTime, now))
                        .orderByDesc(SmsSeckillPromotion::getId)
                        .last("LIMIT 1"));
    }
}
