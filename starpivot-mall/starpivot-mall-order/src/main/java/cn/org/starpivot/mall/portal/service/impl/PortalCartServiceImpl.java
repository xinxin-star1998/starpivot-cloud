package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.api.mall.promotion.dto.SkuPriceRulesDto;
import cn.org.starpivot.api.product.dto.SkuDto;
import cn.org.starpivot.api.product.dto.SpuDto;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.mall.common.MallProductConstants;
import cn.org.starpivot.mall.common.ProductFeignSupport;
import cn.org.starpivot.mall.common.PromotionFeignSupport;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.domain.bo.PortalCartAddBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalCartUpdateBo;
import cn.org.starpivot.mall.portal.domain.model.PortalCartEntry;
import cn.org.starpivot.mall.portal.domain.vo.PortalCartItemVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCartVo;
import cn.org.starpivot.mall.portal.service.PortalAvailableStockService;
import cn.org.starpivot.mall.portal.service.PortalCartService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PortalCartServiceImpl implements PortalCartService {

    private static final TypeReference<Map<String, PortalCartEntry>> CART_TYPE =
            new TypeReference<>() {};

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final ProductFeignSupport productFeignSupport;
    private final PortalAvailableStockService portalAvailableStockService;
    private final PromotionFeignSupport promotionFeignSupport;

    @Override
    @Transactional(readOnly = true)
    public PortalCartVo listCart(Long memberId) {
        Map<String, PortalCartEntry> cartMap = loadCartMap(memberId);
        PortalCartVo vo = new PortalCartVo();
        if (cartMap.isEmpty()) {
            vo.setItems(List.of());
            vo.setCheckedCount(0);
            vo.setCheckedAmount(BigDecimal.ZERO);
            return vo;
        }

        List<Long> skuIds = cartMap.values().stream()
                .map(PortalCartEntry::getSkuId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, SkuDto> skuMap = productFeignSupport.requireSkuMap(skuIds);
        purgeStaleCartEntries(memberId, cartMap, skuMap);
        if (cartMap.isEmpty()) {
            vo.setItems(List.of());
            vo.setCheckedCount(0);
            vo.setCheckedAmount(BigDecimal.ZERO);
            return vo;
        }
        skuIds = cartMap.values().stream()
                .map(PortalCartEntry::getSkuId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<Long> spuIds = skuMap.values().stream()
                .map(SkuDto::getSpuId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, SpuDto> spuMap = productFeignSupport.requireSpuMap(spuIds);
        Map<Long, String> attrMap = loadAttrMap(skuIds);
        Map<Long, Integer> stockMap = loadStockMap(skuIds);
        SkuPriceRulesDto priceRules = promotionFeignSupport.loadSkuPriceRules(skuIds, null);
        Map<Long, BigDecimal> seckillPrices =
                priceRules.getSeckillPrices() == null ? Map.of() : priceRules.getSeckillPrices();

        List<PortalCartItemVo> items = new ArrayList<>();
        BigDecimal checkedAmount = BigDecimal.ZERO;
        int checkedCount = 0;
        for (PortalCartEntry entry : cartMap.values()) {
            PortalCartItemVo item = buildCartItem(entry, skuMap, spuMap, attrMap, stockMap, seckillPrices);
            items.add(item);
            if (Boolean.TRUE.equals(item.getChecked()) && Boolean.TRUE.equals(item.getValid())) {
                checkedCount += item.getQuantity() == null ? 0 : item.getQuantity();
                if (item.getPrice() != null && item.getQuantity() != null) {
                    checkedAmount = checkedAmount.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                }
            }
        }
        vo.setItems(items);
        vo.setCheckedCount(checkedCount);
        vo.setCheckedAmount(checkedAmount);
        return vo;
    }

    @Override
    public void addItem(Long memberId, PortalCartAddBo bo) {
        requireOnSaleSku(bo.getSkuId());
        Map<String, PortalCartEntry> cartMap = loadCartMap(memberId);
        String key = skuKey(bo.getSkuId());
        PortalCartEntry existing = cartMap.get(key);
        if (existing == null) {
            PortalCartEntry entry = new PortalCartEntry();
            entry.setSkuId(bo.getSkuId());
            entry.setQuantity(bo.getQuantity());
            entry.setChecked(Boolean.TRUE);
            cartMap.put(key, entry);
        } else {
            existing.setQuantity(existing.getQuantity() + bo.getQuantity());
        }
        assertStockAvailable(bo.getSkuId(), cartMap.get(key).getQuantity());
        saveCartMap(memberId, cartMap);
    }

    @Override
    public void updateItem(Long memberId, PortalCartUpdateBo bo) {
        Map<String, PortalCartEntry> cartMap = loadCartMap(memberId);
        PortalCartEntry entry = cartMap.get(skuKey(bo.getSkuId()));
        if (entry == null) {
            throw new BizException("购物车中不存在该商品");
        }
        assertStockAvailable(bo.getSkuId(), bo.getQuantity());
        entry.setQuantity(bo.getQuantity());
        if (bo.getChecked() != null) {
            entry.setChecked(bo.getChecked());
        }
        saveCartMap(memberId, cartMap);
    }

    @Override
    public void removeItems(Long memberId, List<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "SKU ID不能为空");
        }
        Map<String, PortalCartEntry> cartMap = loadCartMap(memberId);
        skuIds.forEach(id -> cartMap.remove(skuKey(id)));
        saveCartMap(memberId, cartMap);
    }

    @Override
    public void clearChecked(Long memberId, List<Long> skuIds) {
        Map<String, PortalCartEntry> cartMap = loadCartMap(memberId);
        if (CollectionUtils.isEmpty(skuIds)) {
            cartMap.entrySet().removeIf(e -> Boolean.TRUE.equals(e.getValue().getChecked()));
        } else {
            skuIds.forEach(id -> cartMap.remove(skuKey(id)));
        }
        saveCartMap(memberId, cartMap);
    }

    private PortalCartItemVo buildCartItem(
            PortalCartEntry entry,
            Map<Long, SkuDto> skuMap,
            Map<Long, SpuDto> spuMap,
            Map<Long, String> attrMap,
            Map<Long, Integer> stockMap,
            Map<Long, BigDecimal> seckillPrices) {
        PortalCartItemVo item = new PortalCartItemVo();
        item.setSkuId(entry.getSkuId());
        item.setQuantity(entry.getQuantity());
        item.setChecked(entry.getChecked());

        SkuDto sku = skuMap.get(entry.getSkuId());
        if (sku == null) {
            item.setValid(Boolean.FALSE);
            return item;
        }
        SpuDto spu = sku.getSpuId() == null ? null : spuMap.get(sku.getSpuId());
        boolean onSale = spu != null && Integer.valueOf(MallProductConstants.PUBLISH_STATUS_ON).equals(spu.getPublishStatus());
        item.setValid(onSale);
        item.setSpuId(sku.getSpuId());
        item.setSkuTitle(StringUtils.hasText(sku.getSkuTitle()) ? sku.getSkuTitle() : sku.getSkuName());
        BigDecimal seckill = seckillPrices.get(entry.getSkuId());
        item.setPrice(seckill != null && seckill.compareTo(BigDecimal.ZERO) > 0 ? seckill : sku.getPrice());
        item.setSkuAttr(attrMap.get(entry.getSkuId()));
        item.setStock(stockMap.getOrDefault(entry.getSkuId(), 0));
        if (StringUtils.hasText(sku.getSkuDefaultImg())) {
            item.setSkuDefaultImg(StorageObjectPathUtils.normalizeToObjectName(sku.getSkuDefaultImg()));
        }
        return item;
    }

    private void requireOnSaleSku(Long skuId) {
        productFeignSupport.requireOrderSnapshots(List.of(skuId));
    }

    private void assertStockAvailable(Long skuId, int quantity) {
        int stock = loadStockMap(List.of(skuId)).getOrDefault(skuId, 0);
        if (quantity > stock) {
            throw new BizException("库存不足");
        }
    }

    private Map<Long, String> loadAttrMap(List<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Map.of();
        }
        return productFeignSupport.loadSkuAttrTexts(skuIds);
    }

    private void purgeStaleCartEntries(
            Long memberId, Map<String, PortalCartEntry> cartMap, Map<Long, SkuDto> skuMap) {
        boolean changed = cartMap.entrySet().removeIf(entry -> {
            PortalCartEntry cartEntry = entry.getValue();
            return cartEntry.getSkuId() == null || !skuMap.containsKey(cartEntry.getSkuId());
        });
        if (changed) {
            saveCartMap(memberId, cartMap);
        }
    }

    private Map<Long, Integer> loadStockMap(List<Long> skuIds) {
        return portalAvailableStockService.getAvailableStockMap(skuIds);
    }

    private Map<String, PortalCartEntry> loadCartMap(Long memberId) {
        String json = stringRedisTemplate.opsForValue().get(PortalConstants.cartKey(memberId));
        if (!StringUtils.hasText(json)) {
            return new LinkedHashMap<>();
        }
        try {
            Map<String, PortalCartEntry> map = objectMapper.readValue(json, CART_TYPE);
            return map == null ? new LinkedHashMap<>() : new LinkedHashMap<>(map);
        } catch (Exception e) {
            throw new BizException("购物车数据异常，请稍后重试");
        }
    }

    private void saveCartMap(Long memberId, Map<String, PortalCartEntry> cartMap) {
        try {
            String json = objectMapper.writeValueAsString(cartMap);
            stringRedisTemplate.opsForValue().set(
                    PortalConstants.cartKey(memberId),
                    json,
                    Duration.ofDays(PortalConstants.CART_TTL_DAYS));
        } catch (Exception e) {
            throw new BizException("保存购物车失败");
        }
    }

    private String skuKey(Long skuId) {
        return String.valueOf(skuId);
    }
}
