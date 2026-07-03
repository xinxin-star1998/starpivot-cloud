package cn.org.starpivot.api.fallback;

import cn.org.starpivot.api.mall.ware.WareInternalClient;
import cn.org.starpivot.api.mall.ware.dto.*;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class WareInternalClientFallbackFactory implements FallbackFactory<WareInternalClient> {

    private static final String ACTION = "仓储服务";

    @Override
    public WareInternalClient create(Throwable cause) {
        return new WareInternalClient() {
            @Override
            public Result<Void> addStock(WareSkuAddStockRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Map<Long, Integer>> getWmsAvailableMap(Collection<Long> skuIds) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<List<StockDeductionLineDto>> deductStock(StockDeductRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Long> createFinishedRecordForPaidOrder(OrderTaskFinishedRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Map<Long, Integer>> restoreStockForOrder(Long orderId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Long> inboundForReturn(InboundForReturnRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Long> findOutboundWareId(Long orderId, Long skuId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }
        };
    }
}
