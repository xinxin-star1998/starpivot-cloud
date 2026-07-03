package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.api.mall.ware.dto.StockDeductionLineDto;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.common.PromotionFeignSupport;
import cn.org.starpivot.mall.common.WareFeignSupport;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderItem;
import cn.org.starpivot.mall.oms.entity.OmsOrderOperateHistory;
import cn.org.starpivot.mall.oms.mapper.OmsOrderItemMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderOperateHistoryMapper;
import cn.org.starpivot.mall.oms.service.OmsOrderSettingService;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.service.PortalAvailableStockService;
import cn.org.starpivot.mall.portal.service.PortalMemberIntegrationService;
import cn.org.starpivot.mall.portal.service.PortalStockLockService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Stocklock服务实现类。
 * <p>
 * 实现 {@link PortalStockLockService}，处理Stocklock相关业务。
 * </p>
 * <ul>
 *   <li>{@link Slf4j} — 日志记录</li>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see PortalStockLockService
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class PortalStockLockServiceImpl implements PortalStockLockService {

    private static final TypeReference<Map<String, Integer>> LOCK_PAYLOAD_TYPE = new TypeReference<>() {};

    private final StringRedisTemplate stringRedisTemplate;
    private final WareFeignSupport wareFeignSupport;
    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderItemMapper omsOrderItemMapper;
    private final OmsOrderOperateHistoryMapper omsOrderOperateHistoryMapper;
    private final OmsOrderSettingService omsOrderSettingService;
    private final PortalAvailableStockService portalAvailableStockService;
    private final PromotionFeignSupport promotionFeignSupport;
    private final PortalMemberIntegrationService portalMemberIntegrationService;
    private final ObjectMapper objectMapper;

    private final DefaultRedisScript<Long> reserveScript = loadScript("lua/portal_stock_reserve.lua");
    private final DefaultRedisScript<Long> releaseScript = loadScript("lua/portal_stock_release.lua");

    @Override
    public Map<Long, Integer> getAvailableStockMap(Collection<Long> skuIds) {
        return portalAvailableStockService.getAvailableStockMap(skuIds);
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

        Map<Long, Integer> wmsAvailable = portalAvailableStockService.getWmsAvailableMap(skuQuantities.keySet());
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
    public List<StockDeductionLineDto> confirmForOrder(String orderSn) {
        if (!StringUtils.hasText(orderSn)) {
            return List.of();
        }
        Map<Long, String> skuNameMap = loadSkuNameMap(orderSn);
        Map<Long, Integer> toDeduct = resolveDeductQuantities(orderSn);
        List<StockDeductionLineDto> deductions = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : toDeduct.entrySet()) {
            deductions.addAll(wareFeignSupport.deductStock(
                    entry.getKey(),
                    skuNameMap.get(entry.getKey()),
                    safeQty(entry.getValue())));
        }
        Map<Long, Integer> locked = readLockPayload(orderSn);
        for (Map.Entry<Long, Integer> entry : locked.entrySet()) {
            releaseReserve(entry.getKey(), safeQty(entry.getValue()));
        }
        stringRedisTemplate.delete(PortalConstants.stockLockOrderKey(orderSn));
        stringRedisTemplate.delete(PortalConstants.stockLockConfirmedKey(orderSn));
        cleanupSchedule(orderSn);
        return deductions;
    }

    private Map<Long, String> loadSkuNameMap(String orderSn) {
        OmsOrder order = omsOrderMapper.selectOne(
                Wrappers.<OmsOrder>lambdaQuery().eq(OmsOrder::getOrderSn, orderSn).last("LIMIT 1"));
        if (order == null || order.getId() == null) {
            return Map.of();
        }
        List<OmsOrderItem> items = omsOrderItemMapper.selectList(
                Wrappers.<OmsOrderItem>lambdaQuery().eq(OmsOrderItem::getOrderId, order.getId()));
        Map<Long, String> skuNameMap = new LinkedHashMap<>();
        for (OmsOrderItem item : items) {
            if (item.getSkuId() != null && StringUtils.hasText(item.getSkuName())) {
                skuNameMap.putIfAbsent(item.getSkuId(), item.getSkuName());
            }
        }
        return skuNameMap;
    }

    private Map<Long, Integer> resolveDeductQuantities(String orderSn) {
        Map<Long, Integer> locked = readLockPayload(orderSn);
        if (!locked.isEmpty()) {
            return locked;
        }
        OmsOrder order = omsOrderMapper.selectOne(
                Wrappers.<OmsOrder>lambdaQuery().eq(OmsOrder::getOrderSn, orderSn).last("LIMIT 1"));
        if (order == null || order.getId() == null) {
            return Map.of();
        }
        List<OmsOrderItem> items = omsOrderItemMapper.selectList(
                Wrappers.<OmsOrderItem>lambdaQuery().eq(OmsOrderItem::getOrderId, order.getId()));
        Map<Long, Integer> qtyBySku = new LinkedHashMap<>();
        for (OmsOrderItem item : items) {
            if (item.getSkuId() == null) {
                continue;
            }
            int qty = safeQty(item.getSkuQuantity());
            if (qty <= 0) {
                continue;
            }
            qtyBySku.merge(item.getSkuId(), qty, Integer::sum);
        }
        return qtyBySku;
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
        order.setModifyTime(LocalDateTime.now());
        omsOrderMapper.updateById(order);
        promotionFeignSupport.releaseCoupon(order.getId());
        promotionFeignSupport.releaseSeckillByOrderSn(orderSn);
        portalMemberIntegrationService.restoreForOrder(order);
        saveOperateHistory(order.getId(), PortalConstants.ORDER_STATUS_CLOSED, "system", "超时未支付自动关闭");
        log.info("Closed unpaid order due to stock lock timeout: {}", orderSn);
    }

    private void saveOperateHistory(Long orderId, Integer status, String operator, String note) {
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(orderId);
        history.setOperateMan(operator);
        history.setCreateTime(LocalDateTime.now());
        history.setOrderStatus(status);
        history.setNote(note);
        omsOrderOperateHistoryMapper.insert(history);
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

    private static DefaultRedisScript<Long> loadScript(String classpath) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(classpath)));
        script.setResultType(Long.class);
        return script;
    }
}
