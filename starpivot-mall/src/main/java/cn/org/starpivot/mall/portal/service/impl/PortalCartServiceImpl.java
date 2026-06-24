package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.mall.pms.entity.PmsSkuInfo;
import cn.org.starpivot.mall.pms.entity.PmsSkuSaleAttrValue;
import cn.org.starpivot.mall.pms.entity.PmsSpuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsSkuInfoMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSkuSaleAttrValueMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuInfoMapper;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.domain.bo.PortalCartAddBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalCartUpdateBo;
import cn.org.starpivot.mall.portal.domain.model.PortalCartEntry;
import cn.org.starpivot.mall.portal.domain.vo.PortalCartItemVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCartVo;
import cn.org.starpivot.mall.portal.service.PortalCartService;
import cn.org.starpivot.mall.portal.service.PortalStockLockService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PortalCartServiceImpl implements PortalCartService {

    private static final TypeReference<Map<String, PortalCartEntry>> CART_TYPE =
            new TypeReference<>() {};

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final PmsSkuInfoMapper pmsSkuInfoMapper;
    private final PmsSpuInfoMapper pmsSpuInfoMapper;
    private final PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    private final PortalStockLockService portalStockLockService;

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
        Map<Long, PmsSkuInfo> skuMap = pmsSkuInfoMapper.selectBatchIds(skuIds).stream()
                .collect(Collectors.toMap(PmsSkuInfo::getSkuId, s -> s, (a, b) -> a));
        List<Long> spuIds = skuMap.values().stream()
                .map(PmsSkuInfo::getSpuId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, PmsSpuInfo> spuMap = spuIds.isEmpty()
                ? Map.of()
                : pmsSpuInfoMapper.selectBatchIds(spuIds).stream()
                        .collect(Collectors.toMap(PmsSpuInfo::getId, s -> s, (a, b) -> a));
        Map<Long, String> attrMap = loadSkuAttrMap(skuIds);
        Map<Long, Integer> stockMap = loadStockMap(skuIds);

        List<PortalCartItemVo> items = new ArrayList<>();
        BigDecimal checkedAmount = BigDecimal.ZERO;
        int checkedCount = 0;
        for (PortalCartEntry entry : cartMap.values()) {
            PortalCartItemVo item = buildCartItem(entry, skuMap, spuMap, attrMap, stockMap);
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
        PmsSkuInfo sku = requireOnSaleSku(bo.getSkuId());
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
            Map<Long, PmsSkuInfo> skuMap,
            Map<Long, PmsSpuInfo> spuMap,
            Map<Long, String> attrMap,
            Map<Long, Integer> stockMap) {
        PortalCartItemVo item = new PortalCartItemVo();
        item.setSkuId(entry.getSkuId());
        item.setQuantity(entry.getQuantity());
        item.setChecked(entry.getChecked());

        PmsSkuInfo sku = skuMap.get(entry.getSkuId());
        if (sku == null) {
            item.setValid(Boolean.FALSE);
            return item;
        }
        PmsSpuInfo spu = sku.getSpuId() == null ? null : spuMap.get(sku.getSpuId());
        boolean onSale = spu != null && Integer.valueOf(PortalConstants.PUBLISH_STATUS_ON).equals(spu.getPublishStatus());
        item.setValid(onSale);
        item.setSpuId(sku.getSpuId());
        item.setSkuTitle(StringUtils.hasText(sku.getSkuTitle()) ? sku.getSkuTitle() : sku.getSkuName());
        item.setPrice(sku.getPrice());
        item.setSkuAttr(attrMap.get(entry.getSkuId()));
        item.setStock(stockMap.getOrDefault(entry.getSkuId(), 0));
        if (StringUtils.hasText(sku.getSkuDefaultImg())) {
            item.setSkuDefaultImg(StorageObjectPathUtils.normalizeToObjectName(sku.getSkuDefaultImg()));
        }
        return item;
    }

    private PmsSkuInfo requireOnSaleSku(Long skuId) {
        PmsSkuInfo sku = pmsSkuInfoMapper.selectById(skuId);
        if (sku == null) {
            throw new BizException("SKU不存在");
        }
        PmsSpuInfo spu = sku.getSpuId() == null ? null : pmsSpuInfoMapper.selectById(sku.getSpuId());
        if (spu == null || !Integer.valueOf(PortalConstants.PUBLISH_STATUS_ON).equals(spu.getPublishStatus())) {
            throw new BizException("商品已下架");
        }
        return sku;
    }

    private void assertStockAvailable(Long skuId, int quantity) {
        int stock = loadStockMap(List.of(skuId)).getOrDefault(skuId, 0);
        if (quantity > stock) {
            throw new BizException("库存不足");
        }
    }

    private Map<Long, String> loadSkuAttrMap(List<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Map.of();
        }
        List<PmsSkuSaleAttrValue> attrs = pmsSkuSaleAttrValueMapper.selectList(
                Wrappers.<PmsSkuSaleAttrValue>lambdaQuery()
                        .in(PmsSkuSaleAttrValue::getSkuId, skuIds)
                        .orderByAsc(PmsSkuSaleAttrValue::getAttrSort));
        Map<Long, StringBuilder> builderMap = new LinkedHashMap<>();
        for (PmsSkuSaleAttrValue attr : attrs) {
            if (attr.getSkuId() == null) {
                continue;
            }
            builderMap.computeIfAbsent(attr.getSkuId(), k -> new StringBuilder());
            StringBuilder sb = builderMap.get(attr.getSkuId());
            if (!sb.isEmpty()) {
                sb.append(';');
            }
            sb.append(attr.getAttrName()).append(':').append(attr.getAttrValue());
        }
        Map<Long, String> result = new LinkedHashMap<>();
        builderMap.forEach((k, v) -> result.put(k, v.toString()));
        return result;
    }

    private Map<Long, Integer> loadStockMap(List<Long> skuIds) {
        return portalStockLockService.getAvailableStockMap(skuIds);
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
