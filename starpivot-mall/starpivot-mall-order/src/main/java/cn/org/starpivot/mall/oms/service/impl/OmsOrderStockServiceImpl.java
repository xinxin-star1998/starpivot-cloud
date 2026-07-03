package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.mall.common.ProductFeignSupport;
import cn.org.starpivot.mall.common.WareFeignSupport;
import cn.org.starpivot.mall.oms.service.OmsOrderStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OmsOrderStockServiceImpl implements OmsOrderStockService {

    private final WareFeignSupport wareFeignSupport;
    private final ProductFeignSupport productFeignSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreStockForOrder(Long orderId) {
        if (orderId == null) {
            return;
        }
        Map<Long, Integer> restoredBySku = wareFeignSupport.restoreStockForOrder(orderId);
        restoredBySku.forEach((skuId, qty) -> {
            if (qty != null && qty > 0) {
                productFeignSupport.decrementSaleCount(skuId, qty);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inboundForReturn(Long orderId, Long skuId, int quantity) {
        if (skuId == null || quantity <= 0) {
            return;
        }
        wareFeignSupport.inboundForReturn(orderId, skuId, quantity);
        productFeignSupport.decrementSaleCount(skuId, quantity);
    }

    @Override
    @Transactional(readOnly = true)
    public Long findOutboundWareId(Long orderId, Long skuId) {
        return wareFeignSupport.findOutboundWareId(orderId, skuId);
    }
}
