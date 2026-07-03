package cn.org.starpivot.mall.portal.service.support;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.pms.entity.PmsBrand;
import cn.org.starpivot.mall.pms.entity.PmsSkuInfo;
import cn.org.starpivot.mall.pms.entity.PmsSkuSaleAttrValue;
import cn.org.starpivot.mall.pms.entity.PmsSpuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsBrandMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSkuInfoMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSkuSaleAttrValueMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuInfoMapper;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderItemBo;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 下单链路 SKU/SPU 快照加载。
 */
@Component
@RequiredArgsConstructor
public class PortalOrderSkuSupport {

    private final PmsSkuInfoMapper pmsSkuInfoMapper;
    private final PmsSpuInfoMapper pmsSpuInfoMapper;
    private final PmsBrandMapper pmsBrandMapper;
    private final PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    public record SkuBase(PmsSkuInfo sku, PmsSpuInfo spu, String brandName, String attrs) {
    }

    public record SkuSnapshot(
            PmsSkuInfo sku,
            PmsSpuInfo spu,
            String brandName,
            String attrs,
            BigDecimal lineAmount,
            BigDecimal unitPrice) {
    }

    public Map<Long, SkuBase> loadSkuBaseMap(List<PortalOrderItemBo> orderItems) {
        List<Long> skuIds = orderItems.stream().map(PortalOrderItemBo::getSkuId).distinct().toList();

        List<PmsSkuInfo> skus = skuIds.isEmpty() ? List.of() : pmsSkuInfoMapper.selectBatchIds(skuIds);
        Map<Long, PmsSkuInfo> skuMap = skus.stream()
                .filter(sku -> sku.getSkuId() != null)
                .collect(Collectors.toMap(PmsSkuInfo::getSkuId, sku -> sku, (a, b) -> a));

        List<Long> spuIds = skus.stream()
                .map(PmsSkuInfo::getSpuId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, PmsSpuInfo> spuMap = spuIds.isEmpty()
                ? Map.of()
                : pmsSpuInfoMapper.selectBatchIds(spuIds).stream()
                        .filter(spu -> spu.getId() != null)
                        .collect(Collectors.toMap(PmsSpuInfo::getId, spu -> spu, (a, b) -> a));

        List<Long> brandIds = spuMap.values().stream()
                .map(PmsSpuInfo::getBrandId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> brandNameMap = brandIds.isEmpty()
                ? Map.of()
                : pmsBrandMapper.selectBatchIds(brandIds).stream()
                        .filter(brand -> brand.getBrandId() != null)
                        .collect(Collectors.toMap(PmsBrand::getBrandId, PmsBrand::getName, (a, b) -> a));

        Map<Long, String> attrMap = loadSkuAttrMap(skuIds);

        Map<Long, SkuBase> result = new LinkedHashMap<>();
        for (Long skuId : skuIds) {
            PmsSkuInfo sku = skuMap.get(skuId);
            if (sku == null) {
                throw new BizException("SKU不存在：" + skuId);
            }
            PmsSpuInfo spu = sku.getSpuId() == null ? null : spuMap.get(sku.getSpuId());
            if (spu == null || !Integer.valueOf(PortalConstants.PUBLISH_STATUS_ON).equals(spu.getPublishStatus())) {
                throw new BizException("商品已下架：" + skuId);
            }
            if (sku.getPrice() == null) {
                throw new BizException("商品价格异常：" + skuId);
            }
            String brandName = spu.getBrandId() == null ? null : brandNameMap.get(spu.getBrandId());
            result.put(skuId, new SkuBase(sku, spu, brandName, attrMap.getOrDefault(skuId, "")));
        }
        return result;
    }

    public SkuSnapshot toSnapshot(SkuBase base, int quantity, BigDecimal unitPrice, BigDecimal lineAmount) {
        return new SkuSnapshot(base.sku(), base.spu(), base.brandName(), base.attrs(), lineAmount, unitPrice);
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
            StringBuilder sb = builderMap.computeIfAbsent(attr.getSkuId(), k -> new StringBuilder());
            if (!sb.isEmpty()) {
                sb.append(';');
            }
            sb.append(attr.getAttrName()).append(':').append(attr.getAttrValue());
        }
        Map<Long, String> result = new LinkedHashMap<>();
        builderMap.forEach((skuId, sb) -> result.put(skuId, sb.toString()));
        return result;
    }
}
