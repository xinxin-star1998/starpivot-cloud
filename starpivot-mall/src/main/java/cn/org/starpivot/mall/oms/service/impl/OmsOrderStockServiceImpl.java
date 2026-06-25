package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.mall.oms.entity.OmsOrderItem;
import cn.org.starpivot.mall.oms.mapper.OmsOrderItemMapper;
import cn.org.starpivot.mall.oms.service.OmsOrderStockService;
import cn.org.starpivot.mall.pms.mapper.PmsSkuInfoMapper;
import cn.org.starpivot.mall.wms.entity.WmsWareOrderTask;
import cn.org.starpivot.mall.wms.entity.WmsWareOrderTaskDetail;
import cn.org.starpivot.mall.wms.mapper.WmsWareOrderTaskDetailMapper;
import cn.org.starpivot.mall.wms.mapper.WmsWareOrderTaskMapper;
import cn.org.starpivot.mall.wms.mapper.WmsWareSkuMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OmsOrderStockServiceImpl implements OmsOrderStockService {

    private final WmsWareOrderTaskMapper wmsWareOrderTaskMapper;
    private final WmsWareOrderTaskDetailMapper wmsWareOrderTaskDetailMapper;
    private final WmsWareSkuMapper wmsWareSkuMapper;
    private final PmsSkuInfoMapper pmsSkuInfoMapper;
    private final OmsOrderItemMapper omsOrderItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreStockForOrder(Long orderId) {
        if (orderId == null) {
            return;
        }
        Map<Long, Map<Long, Integer>> wareQtyBySku = loadOutboundLines(orderId);
        if (wareQtyBySku.isEmpty()) {
            wareQtyBySku = fallbackFromOrderItems(orderId);
        }
        wareQtyBySku.forEach((skuId, wareMap) -> {
            int totalQty = wareMap.values().stream().mapToInt(Integer::intValue).sum();
            wareMap.forEach((wareId, qty) -> {
                if (wareId != null && wareId > 0) {
                    wmsWareSkuMapper.addStock(skuId, wareId, qty);
                }
            });
            if (totalQty > 0) {
                pmsSkuInfoMapper.decrementSaleCount(skuId, totalQty);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inboundForReturn(Long orderId, Long skuId, int quantity) {
        if (skuId == null || quantity <= 0) {
            return;
        }
        Long wareId = findOutboundWareId(orderId, skuId);
        if (wareId == null) {
            List<Long> wareIds = wmsWareSkuMapper.listWareIdHasStock(skuId);
            wareId = CollectionUtils.isEmpty(wareIds) ? 1L : wareIds.get(0);
        }
        wmsWareSkuMapper.addStock(skuId, wareId, quantity);
        pmsSkuInfoMapper.decrementSaleCount(skuId, quantity);
    }

    @Override
    @Transactional(readOnly = true)
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
        List<OmsOrderItem> items = omsOrderItemMapper.selectList(
                Wrappers.<OmsOrderItem>lambdaQuery().eq(OmsOrderItem::getOrderId, orderId));
        Map<Long, Map<Long, Integer>> result = new LinkedHashMap<>();
        for (OmsOrderItem item : items) {
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
}
