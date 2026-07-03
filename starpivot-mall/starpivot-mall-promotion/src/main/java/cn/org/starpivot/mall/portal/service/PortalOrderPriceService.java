package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.api.product.dto.SkuDto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 下单价格解析（秒杀价等营销价）。
 */
public interface PortalOrderPriceService {

    /** 解析 SKU 下单单价，无活动则返回 SKU 原价 */
    BigDecimal resolveUnitPrice(Long skuId, SkuDto sku);

    /** 批量解析 */
    Map<Long, BigDecimal> resolveUnitPriceMap(Map<Long, SkuDto> skuMap);
}
