package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.oms.service.OmsOrderSettingService;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.service.PortalStockLockService;
import cn.org.starpivot.mall.wms.entity.WmsWareSku;
import cn.org.starpivot.mall.wms.mapper.WmsWareSkuMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortalStockLockServiceImpl implements PortalStockLockService {

    private static final TypeReference<Map<String, Integer>> LOCK_PAYLOAD_TYPE = new TypeReference<>() {};

    private final StringRedisTemplate stringRedisTemplate;
    private final WmsWareSkuMapper wmsWareSkuMapper;
    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderSettingService omsOrderSettingService;
    private final ObjectMapper objectMapper;

    private final DefaultRedisScript<Long> reserveScript = loadScript("lua/portal_stock_reserve.lua");
    private final DefaultRedisScript<Long> releaseScript = loadScript("lua/portal_stock_release.lua");

    @Override
    public Map<Long, Integer> getAvailableStockMap(Collection<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Map.of();
        }
        Map<Long, Integer> wmsAvailable = loadWmsAvailableMap(skuIds);
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

    @Override
    public void lockForOrder(String orderSn, Map<Long, Integer> skuQuantities) {
        if (!StringUtils.hasText(orderSn) || CollectionUtils.isEmpty(skuQuantities)) {
            throw new BizException("库存锁定参数无效");
        }
        String existing = stringRedisTemplate.opsForValue().get(PortalConstants.stockLockOrderKey(orderSn));
        if (StringUtils.hasText(existing)) {
            return;
        }

        Map<Long, Integer> wmsAvailable = loadWmsAvailableMap(skuQuantities.keySet());
        List<Long> lockedSkuIds = new ArrayList<>();
        Map<String, Integer> payload = new LinkedHashMap<>();

        try {
            for (Map.Entry<Long, Integer> entry : skuQuantities.entrySet()) {
                Long skuId = entry.getKey();
                int qty = safeQty(entry.getValue());
                if (skuId == null || qty <= 0) {
                    continue;
                }
                int maxAvailable = wmsAvailable.getOrDefault(skuId, 0);
                if (!tryReserve(skuId, qty, maxAvailable)) {
                    throw new BizException("库存不足，请稍后重试");
                }
                lockedSkuIds.add(skuId);
                payload.put(String.valueOf(skuId), qty);
            }

            if (payload.isEmpty()) {
                throw new BizException("下单商品不能为空");
            }

            Duration ttl = unpaidLockTtl();
            long expireAtMs = System.currentTimeMillis() + ttl.toMillis();
            stringRedisTemplate
                    .opsForValue()
                    .set(PortalConstants.stockLockOrderKey(orderSn), writePayload(payload), ttl);
            stringRedisTemplate
                    .opsForZSet()
                    .add(PortalConstants.STOCK_LOCK_EXPIRY_ZSET, orderSn, expireAtMs);
        } catch (RuntimeException ex) {
            rollbackReserve(orderSn, lockedSkuIds, skuQuantities);
            if (ex instanceof BizException bizException) {
                throw bizException;
            }
            throw ex;
        }
    }

    @Override
    public void releaseForOrder(String orderSn) {
        if (!StringUtils.hasText(orderSn)) {
            return;
        }
        Map<Long, Integer> locked = readLockPayload(orderSn);
        if (locked.isEmpty()) {
            cleanupSchedule(orderSn);
            return;
        }
        for (Map.Entry<Long, Integer> entry : locked.entrySet()) {
            releaseReserve(entry.getKey(), safeQty(entry.getValue()));
        }
        stringRedisTemplate.delete(PortalConstants.stockLockOrderKey(orderSn));
        stringRedisTemplate.delete(PortalConstants.stockLockConfirmedKey(orderSn));
        cleanupSchedule(orderSn);
    }

    @Override
    public void confirmForOrder(String orderSn) {
        if (!StringUtils.hasText(orderSn)) {
            return;
        }
        cleanupSchedule(orderSn);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(PortalConstants.stockLockOrderKey(orderSn)))) {
            stringRedisTemplate
                    .opsForValue()
                    .set(PortalConstants.stockLockConfirmedKey(orderSn), "1", Duration.ofDays(7));
        }
    }

    @Override
    public void releaseExpiredLocks() {
        long now = System.currentTimeMillis();
        Set<String> expiredOrderSns = stringRedisTemplate
                .opsForZSet()
                .rangeByScore(PortalConstants.STOCK_LOCK_EXPIRY_ZSET, 0, now);
        if (expiredOrderSns == null || expiredOrderSns.isEmpty()) {
            return;
        }
        for (String orderSn : expiredOrderSns) {
            try {
                if (shouldReleaseExpiredLock(orderSn)) {
                    releaseForOrder(orderSn);
                    closeUnpaidOrderIfNeeded(orderSn);
                } else {
                    cleanupSchedule(orderSn);
                }
            } catch (Exception ex) {
                log.warn("Failed to release expired stock lock for orderSn={}", orderSn, ex);
            }
        }
    }

    private boolean shouldReleaseExpiredLock(String orderSn) {
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(PortalConstants.stockLockConfirmedKey(orderSn)))) {
            return false;
        }
        OmsOrder order = omsOrderMapper.selectOne(
                Wrappers.<OmsOrder>lambdaQuery().eq(OmsOrder::getOrderSn, orderSn).last("LIMIT 1"));
        if (order == null) {
            return true;
        }
        return Integer.valueOf(PortalConstants.ORDER_STATUS_UNPAID).equals(order.getStatus());
    }

    private void closeUnpaidOrderIfNeeded(String orderSn) {
        OmsOrder order = omsOrderMapper.selectOne(
                Wrappers.<OmsOrder>lambdaQuery().eq(OmsOrder::getOrderSn, orderSn).last("LIMIT 1"));
        if (order == null
                || !Integer.valueOf(PortalConstants.ORDER_STATUS_UNPAID).equals(order.getStatus())) {
            return;
        }
        order.setStatus(PortalConstants.ORDER_STATUS_CLOSED);
        omsOrderMapper.updateById(order);
        log.info("Closed unpaid order due to stock lock timeout: {}", orderSn);
    }

    private void rollbackReserve(String orderSn, List<Long> lockedSkuIds, Map<Long, Integer> skuQuantities) {
        for (Long skuId : lockedSkuIds) {
            releaseReserve(skuId, safeQty(skuQuantities.get(skuId)));
        }
        stringRedisTemplate.delete(PortalConstants.stockLockOrderKey(orderSn));
        cleanupSchedule(orderSn);
    }

    private boolean tryReserve(Long skuId, int qty, int maxAvailable) {
        Long result = stringRedisTemplate.execute(
                reserveScript,
                List.of(PortalConstants.stockReservedKey(skuId)),
                String.valueOf(qty),
                String.valueOf(maxAvailable));
        return result != null && result > 0;
    }

    private void releaseReserve(Long skuId, int qty) {
        if (skuId == null || qty <= 0) {
            return;
        }
        stringRedisTemplate.execute(
                releaseScript, List.of(PortalConstants.stockReservedKey(skuId)), String.valueOf(qty));
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

    private Map<Long, Integer> loadWmsAvailableMap(Collection<Long> skuIds) {
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

    private Map<Long, Integer> readLockPayload(String orderSn) {
        String json = stringRedisTemplate.opsForValue().get(PortalConstants.stockLockOrderKey(orderSn));
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            Map<String, Integer> raw = objectMapper.readValue(json, LOCK_PAYLOAD_TYPE);
            Map<Long, Integer> result = new LinkedHashMap<>();
            raw.forEach((skuId, qty) -> result.put(Long.valueOf(skuId), safeQty(qty)));
            return result;
        } catch (JsonProcessingException ex) {
            log.warn("Invalid stock lock payload for orderSn={}", orderSn, ex);
            return Map.of();
        }
    }

    private String writePayload(Map<String, Integer> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new BizException("库存锁定数据序列化失败");
        }
    }

    private Duration unpaidLockTtl() {
        try {
            Integer minutes = omsOrderSettingService.getSetting().getNormalOrderOvertime();
            if (minutes != null && minutes > 0) {
                return Duration.ofMinutes(minutes);
            }
        } catch (Exception ex) {
            log.debug("Use default unpaid lock ttl, order setting unavailable", ex);
        }
        return Duration.ofMinutes(PortalConstants.DEFAULT_UNPAID_LOCK_MINUTES);
    }

    private void cleanupSchedule(String orderSn) {
        stringRedisTemplate.opsForZSet().remove(PortalConstants.STOCK_LOCK_EXPIRY_ZSET, orderSn);
    }

    private static int safeQty(Integer value) {
        return value == null || value < 0 ? 0 : value;
    }

    private static int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private static DefaultRedisScript<Long> loadScript(String classpath) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(classpath)));
        script.setResultType(Long.class);
        return script;
    }
}
