package cn.org.starpivot.mall.common;

import cn.org.starpivot.api.mall.ware.WareInternalClient;
import cn.org.starpivot.api.mall.ware.dto.InboundForReturnRequest;
import cn.org.starpivot.api.mall.ware.dto.OrderTaskFinishedRequest;
import cn.org.starpivot.api.mall.ware.dto.StockDeductRequest;
import cn.org.starpivot.api.mall.ware.dto.StockDeductionLineDto;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WareFeignSupport {

    private static final String ACTION = "仓储服务";

    private final WareInternalClient wareInternalClient;

    public Map<Long, Integer> getWmsAvailableMap(Collection<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Map.of();
        }
        Map<Long, Integer> data = unwrap(wareInternalClient.getWmsAvailableMap(skuIds), "WMS库存查询失败");
        return data == null ? Map.of() : data;
    }

    public List<StockDeductionLineDto> deductStock(Long skuId, String skuName, int quantity) {
        StockDeductRequest request = new StockDeductRequest();
        request.setSkuId(skuId);
        request.setSkuName(skuName);
        request.setQuantity(quantity);
        return unwrap(wareInternalClient.deductStock(request), "库存扣减失败");
    }

    public Long createFinishedRecordForPaidOrder(Long orderId, List<StockDeductionLineDto> deductions) {
        OrderTaskFinishedRequest request = new OrderTaskFinishedRequest();
        request.setOrderId(orderId);
        request.setDeductions(deductions);
        return unwrap(wareInternalClient.createFinishedRecordForPaidOrder(request), "出库工作单生成失败");
    }

    public Map<Long, Integer> restoreStockForOrder(Long orderId) {
        if (orderId == null) {
            return Map.of();
        }
        Map<Long, Integer> data = unwrap(wareInternalClient.restoreStockForOrder(orderId), "库存回滚失败");
        return data == null ? Map.of() : data;
    }

    public Long inboundForReturn(Long orderId, Long skuId, int quantity) {
        InboundForReturnRequest request = new InboundForReturnRequest();
        request.setOrderId(orderId);
        request.setSkuId(skuId);
        request.setQuantity(quantity);
        return unwrap(wareInternalClient.inboundForReturn(request), "退货入库失败");
    }

    public Long findOutboundWareId(Long orderId, Long skuId) {
        return unwrap(wareInternalClient.findOutboundWareId(orderId, skuId), "出库仓库查询失败");
    }

    private <T> T unwrap(Result<T> result, String notFoundMessage) {
        if (result == null) {
            throw new BizException(ACTION + "不可用");
        }
        if (!result.isSuccess()) {
            if (result.getCode() == ErrorCode.NOT_FOUND) {
                throw new BizException(notFoundMessage);
            }
            throw new BizException(result.getMessage());
        }
        return result.getData();
    }
}
