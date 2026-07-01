package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.config.MallSeckillProperties;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.service.PortalSeckillStockService;
import cn.org.starpivot.mall.sms.entity.SmsSeckillSkuRelation;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortalSeckillStockServiceImpl implements PortalSeckillStockService {

    private final StringRedisTemplate stringRedisTemplate;
    private final MallSeckillProperties mallSeckillProperties;
    private final ObjectMapper objectMapper;

    private final DefaultRedisScript<Long> reserveScript = loadScript("lua/portal_seckill_reserve.lua");
    private final DefaultRedisScript<Long> releaseScript = loadScript("lua/portal_seckill_release.lua");

    @Override
    public void warmup(Long promotionId, List<SmsSeckillSkuRelation> relations) {
        if (promotionId == null || CollectionUtils.isEmpty(relations)) {
            return;
        }
        for (SmsSeckillSkuRelation relation : relations) {
            if (relation.getPromotionSessionId() == null || relation.getSkuId() == null) {
                continue;
            }
            String key = PortalConstants.seckillStockKey(
                    promotionId, relation.getPromotionSessionId(), relation.getSkuId());
            int count = relation.getSeckillCount() != null ? relation.getSeckillCount() : 0;
            stringRedisTemplate.opsForValue().setIfAbsent(key, String.valueOf(count));
        }
    }

    @Override
    public int getRemainStock(Long promotionId, Long sessionId, Long skuId) {
        String value = stringRedisTemplate.opsForValue().get(
                PortalConstants.seckillStockKey(promotionId, sessionId, skuId));
        if (!StringUtils.hasText(value)) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(value.trim()));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    @Override
    public boolean reserve(Long promotionId, Long sessionId, Long skuId, Long memberId, int quantity, int seckillLimit) {
        if (quantity <= 0) {
            throw new BizException("购买数量无效");
        }
        String stockKey = PortalConstants.seckillStockKey(promotionId, sessionId, skuId);
        String limitKey = PortalConstants.seckillLimitKey(promotionId, sessionId, skuId, memberId);
        Long result = stringRedisTemplate.execute(
                reserveScript,
                List.of(stockKey, limitKey),
                String.valueOf(quantity),
                String.valueOf(Math.max(0, seckillLimit)));
        if (result == null) {
            throw new BizException("秒杀扣减失败，请重试");
        }
        if (result == -1L) {
            throw new BizException("秒杀库存不足");
        }
        if (result == -2L) {
            throw new BizException("已超过本场秒杀限购数量");
        }
        if (result < 0) {
            throw new BizException("秒杀扣减失败，请重试");
        }
        return true;
    }

    @Override
    public void release(Long promotionId, Long sessionId, Long skuId, Long memberId, int quantity, int seckillLimit) {
        if (quantity <= 0) {
            return;
        }
        String stockKey = PortalConstants.seckillStockKey(promotionId, sessionId, skuId);
        String limitKey = PortalConstants.seckillLimitKey(promotionId, sessionId, skuId, memberId);
        stringRedisTemplate.execute(
                releaseScript,
                List.of(stockKey, limitKey),
                String.valueOf(quantity),
                String.valueOf(Math.max(0, seckillLimit)));
    }

    @Override
    public void bindOrder(
            String orderSn,
            Long promotionId,
            Long sessionId,
            Long skuId,
            Long memberId,
            int quantity,
            int seckillLimit) {
        if (!StringUtils.hasText(orderSn)) {
            return;
        }
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "promotionId", promotionId,
                    "sessionId", sessionId,
                    "skuId", skuId,
                    "memberId", memberId,
                    "quantity", quantity,
                    "limit", Math.max(0, seckillLimit)));
            int minutes = Math.max(1, mallSeckillProperties.getUnpaidLockMinutes());
            stringRedisTemplate
                    .opsForValue()
                    .set(PortalConstants.seckillOrderKey(orderSn), payload, Duration.ofMinutes(minutes));
        } catch (JsonProcessingException ex) {
            log.warn("Failed to bind seckill order: {}", orderSn, ex);
        }
    }

    @Override
    public void releaseByOrderSn(String orderSn) {
        if (!StringUtils.hasText(orderSn)) {
            return;
        }
        String key = PortalConstants.seckillOrderKey(orderSn);
        String payload = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(payload)) {
            return;
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(payload, Map.class);
            Long promotionId = toLong(map.get("promotionId"));
            Long sessionId = toLong(map.get("sessionId"));
            Long skuId = toLong(map.get("skuId"));
            Long memberId = toLong(map.get("memberId"));
            int quantity = toInt(map.get("quantity"));
            int limit = toInt(map.get("limit"));
            if (promotionId != null && sessionId != null && skuId != null && memberId != null && quantity > 0) {
                release(promotionId, sessionId, skuId, memberId, quantity, limit);
            }
        } catch (Exception ex) {
            log.warn("Failed to release seckill stock for orderSn={}", orderSn, ex);
        } finally {
            stringRedisTemplate.delete(key);
        }
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static DefaultRedisScript<Long> loadScript(String classpath) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(classpath)));
        script.setResultType(Long.class);
        return script;
    }
}
