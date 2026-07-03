package cn.org.starpivot.mall.portal.service.support;

import cn.org.starpivot.api.product.dto.SkuOrderSnapshotDto;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.common.ProductFeignSupport;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderItemBo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 下单链路 SKU/SPU 快照加载。
 */
@Component
@RequiredArgsConstructor
public class PortalOrderSkuSupport {

    private final ProductFeignSupport productFeignSupport;

    public record SkuBase(SkuOrderSnapshotDto snapshot) {
    }

    public record SkuSnapshot(
            SkuOrderSnapshotDto snapshot,
            BigDecimal lineAmount,
            BigDecimal unitPrice) {
    }

    public Map<Long, SkuBase> loadSkuBaseMap(List<PortalOrderItemBo> orderItems) {
        List<Long> skuIds = orderItems.stream().map(PortalOrderItemBo::getSkuId).distinct().toList();
        if (CollectionUtils.isEmpty(skuIds)) {
            return Map.of();
        }
        Map<Long, SkuOrderSnapshotDto> snapshotMap = productFeignSupport.requireOrderSnapshots(skuIds);
        Map<Long, SkuBase> result = new LinkedHashMap<>();
        for (Long skuId : skuIds) {
            SkuOrderSnapshotDto snapshot = snapshotMap.get(skuId);
            if (snapshot == null) {
                throw new BizException("SKU不存在：" + skuId);
            }
            result.put(skuId, new SkuBase(snapshot));
        }
        return result;
    }

    public SkuSnapshot toSnapshot(SkuBase base, int quantity, BigDecimal unitPrice, BigDecimal lineAmount) {
        return new SkuSnapshot(base.snapshot(), lineAmount, unitPrice);
    }
}
