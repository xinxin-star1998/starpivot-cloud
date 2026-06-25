package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.service.PortalAvailableStockService;
import cn.org.starpivot.mall.wms.entity.WmsWareSku;
import cn.org.starpivot.mall.wms.mapper.WmsWareSkuMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortalAvailableStockServiceImpl implements PortalAvailableStockService {

    private final StringRedisTemplate stringRedisTemplate;
    private final WmsWareSkuMapper wmsWareSkuMapper;

    private static int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    @Override
    public Map<Long, Integer> getWmsAvailableMap(Collection<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Map.of();
        }
        List<WmsWareSku> stocks = wmsWareSkuMapper.selectList(
                Wrappers.<WmsWareSku>lambdaQuery().in(WmsWareSku::getSkuId, skuIds));
        Map<Long, Integer> stockMap = new LinkedHashMap<>();
        for (WmsWareSku wareSku : stocks) {
            if (wareSku.getSkuId() == null) {
                continue;
            }
            int available = Math.max(0, safeInt(wareSku.getStock()) - safeInt(wareSku.getStockLocked()));
            stockMap.merge(wareSku.getSkuId(), available, Integer::sum);
        }
        return stockMap;
    }

    @Override
    public Map<Long, Integer> getAvailableStockMap(Collection<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Map.of();
        }
        Map<Long, Integer> wmsAvailable = getWmsAvailableMap(skuIds);
        Map<Long, Integer> result = new LinkedHashMap<>();
        for (Long skuId : skuIds) {
            if (skuId == null) {
                continue;
            }
            int wmsStock = wmsAvailable.getOrDefault(skuId, 0);
            int reserved = getReservedQty(skuId);
            result.put(skuId, Math.max(0, wmsStock - reserved));
        }
        return result;
    }

    private int getReservedQty(Long skuId) {
        String value = stringRedisTemplate.opsForValue().get(PortalConstants.stockReservedKey(skuId));
        if (!StringUtils.hasText(value)) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
