package cn.org.starpivot.mall.wms.service.impl;

import cn.org.starpivot.api.mall.order.dto.OrderPaidLineDto;
import cn.org.starpivot.api.mall.order.dto.OrderPaidMessage;
import cn.org.starpivot.api.mall.ware.dto.StockDeductRequest;
import cn.org.starpivot.api.mall.ware.dto.StockDeductionLineDto;
import cn.org.starpivot.mall.ware.internal.WareInternalService;
import cn.org.starpivot.mall.wms.service.WmsOrderPaidFulfillmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WmsOrderPaidFulfillmentServiceImpl implements WmsOrderPaidFulfillmentService {

    private final WareInternalService wareInternalService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fulfill(OrderPaidMessage message) {
        if (message == null || message.getOrderId() == null) {
            log.warn("忽略无效 OrderPaidMessage");
            return;
        }
        List<StockDeductionLineDto> deductions = new ArrayList<>();
        if (!CollectionUtils.isEmpty(message.getLines())) {
            for (OrderPaidLineDto line : message.getLines()) {
                if (line == null || line.getSkuId() == null || line.getQuantity() == null || line.getQuantity() <= 0) {
                    continue;
                }
                deductions.addAll(wareInternalService.deductStock(
                        toDeductRequest(line.getSkuId(), line.getSkuName(), line.getQuantity())));
            }
        }
        Long taskId = wareInternalService.createFinishedRecordForPaidOrder(message.getOrderId(), deductions);
        log.info("订单支付库存履约完成 orderId={}, orderSn={}, taskId={}, deductionLines={}",
                message.getOrderId(), message.getOrderSn(), taskId, deductions.size());
    }

    private static StockDeductRequest toDeductRequest(Long skuId, String skuName, int quantity) {
        StockDeductRequest request = new StockDeductRequest();
        request.setSkuId(skuId);
        request.setSkuName(skuName);
        request.setQuantity(quantity);
        return request;
    }
}
