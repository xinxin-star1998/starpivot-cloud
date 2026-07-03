package cn.org.starpivot.mall.ware.internal;

import cn.org.starpivot.api.mall.order.dto.OrderItemInternalDto;
import cn.org.starpivot.api.mall.ware.dto.StockDeductRequest;
import cn.org.starpivot.api.mall.ware.dto.StockDeductionLineDto;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.common.OrderFeignSupport;
import cn.org.starpivot.mall.wms.entity.WmsWareOrderTask;
import cn.org.starpivot.mall.wms.entity.WmsWareOrderTaskDetail;
import cn.org.starpivot.mall.wms.entity.WmsWareSku;
import cn.org.starpivot.mall.wms.mapper.WmsWareOrderTaskDetailMapper;
import cn.org.starpivot.mall.wms.mapper.WmsWareOrderTaskMapper;
import cn.org.starpivot.mall.wms.mapper.WmsWareSkuMapper;
import cn.org.starpivot.mall.wms.service.WmsStockWarningService;
import cn.org.starpivot.mall.wms.service.WmsWareOrderTaskService;
import cn.org.starpivot.mall.wms.service.WmsWareSkuService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class WareInternalService {

    private final WmsWareSkuService wmsWareSkuService;
    private final WmsWareSkuMapper wmsWareSkuMapper;
    private final WmsStockWarningService wmsStockWarningService;
    private final WmsWareOrderTaskService wmsWareOrderTaskService;
    private final WmsWareOrderTaskMapper wmsWareOrderTaskMapper;
    private final WmsWareOrderTaskDetailMapper wmsWareOrderTaskDetailMapper;
    private final OrderFeignSupport orderFeignSupport;

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

    @Transactional(rollbackFor = Exception.class)
    public List<StockDeductionLineDto> deductStock(StockDeductRequest request) {
        if (request == null || request.getSkuId() == null || request.getQuantity() == null || request.getQuantity() <= 0) {
            return List.of();
        }
        return deductSkuFromWms(request.getSkuId(), request.getSkuName(), request.getQuantity());
    }

    public Long createFinishedRecordForPaidOrder(Long orderId, List<StockDeductionLineDto> deductions) {
        return wmsWareOrderTaskService.createFinishedRecordForPaidOrder(orderId, deductions);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<Long, Integer> restoreStockForOrder(Long orderId) {
        if (orderId == null) {
            return Map.of();
        }
        Map<Long, Map<Long, Integer>> wareQtyBySku = loadOutboundLines(orderId);
        if (wareQtyBySku.isEmpty()) {
            wareQtyBySku = fallbackFromOrderItems(orderId);
        }
        Map<Long, Integer> restoredBySku = new LinkedHashMap<>();
        wareQtyBySku.forEach((skuId, wareMap) -> {
            int totalQty = 0;
            for (Map.Entry<Long, Integer> entry : wareMap.entrySet()) {
                Long wareId = entry.getKey();
                int qty = safeQty(entry.getValue());
                if (wareId != null && wareId > 0 && qty > 0) {
                    wmsWareSkuMapper.addStock(skuId, wareId, qty);
                    totalQty += qty;
                }
            }
            if (totalQty > 0) {
                restoredBySku.put(skuId, totalQty);
            }
        });
        return restoredBySku;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long inboundForReturn(Long orderId, Long skuId, int quantity) {
        if (skuId == null || quantity <= 0) {
            return null;
        }
        Long wareId = findOutboundWareId(orderId, skuId);
        if (wareId == null) {
            List<Long> wareIds = wmsWareSkuMapper.listWareIdHasStock(skuId);
            wareId = CollectionUtils.isEmpty(wareIds) ? 1L : wareIds.get(0);
        }
        wmsWareSkuMapper.addStock(skuId, wareId, quantity);
        return wareId;
    }

    public Long findOutboundWareId(Long orderId, Long skuId) {
        WmsWareOrderTask task = wmsWareOrderTaskMapper.selectOne(
                Wrappers.<WmsWareOrderTask>lambdaQuery()
                        .eq(WmsWareOrderTask::getOrderId, orderId)
                        .last("LIMIT 1"));
        if (task == null) {
            return null;
        }
        List<WmsWareOrderTaskDetail> details = wmsWareOrderTaskDetailMapper.listByTaskId(task.getId());
        return details.stream()
                .filter(d -> skuId.equals(d.getSkuId()) && d.getWareId() != null)
                .map(WmsWareOrderTaskDetail::getWareId)
                .findFirst()
                .orElse(null);
    }

    public void addStock(Long skuId, Long wareId, Integer quantity) {
        wmsWareSkuService.addStock(skuId, wareId, quantity);
    }

    private List<StockDeductionLineDto> deductSkuFromWms(Long skuId, String skuName, int qty) {
        List<StockDeductionLineDto> lines = new ArrayList<>();
        int remaining = qty;
        while (remaining > 0) {
            List<WmsWareSku> stocks = wmsWareSkuMapper.selectList(
                    Wrappers.<WmsWareSku>lambdaQuery()
                            .eq(WmsWareSku::getSkuId, skuId)
                            .orderByDesc(WmsWareSku::getStock));
            boolean deducted = false;
            for (WmsWareSku wareSku : stocks) {
                if (wareSku.getWareId() == null) {
                    continue;
                }
                int available = Math.max(0, safeInt(wareSku.getStock()) - safeInt(wareSku.getStockLocked()));
                if (available <= 0) {
                    continue;
                }
                int take = Math.min(remaining, available);
                if (wmsWareSkuMapper.deductAvailableStock(skuId, wareSku.getWareId(), take) > 0) {
                    wmsStockWarningService.checkAndCreatePurchaseDemand(skuId, wareSku.getWareId());
                    StockDeductionLineDto line = new StockDeductionLineDto();
                    line.setSkuId(skuId);
                    line.setSkuName(StringUtils.hasText(skuName) ? skuName : wareSku.getSkuName());
                    line.setWareId(wareSku.getWareId());
                    line.setQuantity(take);
                    lines.add(line);
                    remaining -= take;
                    deducted = true;
                    break;
                }
            }
            if (!deducted) {
                throw new BizException("SKU[" + skuId + "]库存扣减失败，请检查仓库库存");
            }
        }
        return lines;
    }

    private Map<Long, Map<Long, Integer>> loadOutboundLines(Long orderId) {
        WmsWareOrderTask task = wmsWareOrderTaskMapper.selectOne(
                Wrappers.<WmsWareOrderTask>lambdaQuery()
                        .eq(WmsWareOrderTask::getOrderId, orderId)
                        .last("LIMIT 1"));
        if (task == null) {
            return Map.of();
        }
        List<WmsWareOrderTaskDetail> details = wmsWareOrderTaskDetailMapper.listByTaskId(task.getId());
        Map<Long, Map<Long, Integer>> result = new LinkedHashMap<>();
        for (WmsWareOrderTaskDetail detail : details) {
            if (detail.getSkuId() == null || detail.getSkuNum() == null || detail.getSkuNum() <= 0) {
                continue;
            }
            Long wareId = detail.getWareId() != null ? detail.getWareId() : 0L;
            result.computeIfAbsent(detail.getSkuId(), k -> new LinkedHashMap<>())
                    .merge(wareId, detail.getSkuNum(), Integer::sum);
        }
        return result;
    }

    private Map<Long, Map<Long, Integer>> fallbackFromOrderItems(Long orderId) {
        List<OrderItemInternalDto> items = orderFeignSupport.requireOrderItems(orderId);
        Map<Long, Map<Long, Integer>> result = new LinkedHashMap<>();
        for (OrderItemInternalDto item : items) {
            if (item.getSkuId() == null || item.getSkuQuantity() == null || item.getSkuQuantity() <= 0) {
                continue;
            }
            Long wareId = findOutboundWareId(orderId, item.getSkuId());
            if (wareId == null) {
                List<Long> wareIds = wmsWareSkuMapper.listWareIdHasStock(item.getSkuId());
                wareId = CollectionUtils.isEmpty(wareIds) ? 1L : wareIds.get(0);
            }
            result.computeIfAbsent(item.getSkuId(), k -> new LinkedHashMap<>())
                    .merge(wareId, item.getSkuQuantity(), Integer::sum);
        }
        return result;
    }

    private static int safeQty(Integer value) {
        return value == null || value < 0 ? 0 : value;
    }

    private static int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
