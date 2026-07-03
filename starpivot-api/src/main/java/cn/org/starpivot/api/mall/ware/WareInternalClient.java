package cn.org.starpivot.api.mall.ware;

import cn.org.starpivot.api.fallback.WareInternalClientFallbackFactory;
import cn.org.starpivot.api.mall.ware.dto.*;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 仓储服务内部 Feign 客户端（供 starpivot-mall 等调用）。
 */
@FeignClient(
        name = "starpivot-mall-ware",
        contextId = "wareInternalClient",
        path = "/api/${starpivot.api.version:v1}",
        fallbackFactory = WareInternalClientFallbackFactory.class)
public interface WareInternalClient {

    @PostMapping("/internal/ware/sku/add-stock")
    Result<Void> addStock(@RequestBody WareSkuAddStockRequest request);

    @PostMapping("/internal/ware/stock/wms-available")
    Result<Map<Long, Integer>> getWmsAvailableMap(@RequestBody Collection<Long> skuIds);

    @PostMapping("/internal/ware/stock/deduct")
    Result<List<StockDeductionLineDto>> deductStock(@RequestBody StockDeductRequest request);

    @PostMapping("/internal/ware/order-task/finished-for-paid")
    Result<Long> createFinishedRecordForPaidOrder(@RequestBody OrderTaskFinishedRequest request);

    @PostMapping("/internal/ware/stock/restore-for-order/{orderId}")
    Result<Map<Long, Integer>> restoreStockForOrder(@PathVariable("orderId") Long orderId);

    @PostMapping("/internal/ware/stock/inbound-for-return")
    Result<Long> inboundForReturn(@RequestBody InboundForReturnRequest request);

    @GetMapping("/internal/ware/outbound-ware")
    Result<Long> findOutboundWareId(@RequestParam("orderId") Long orderId, @RequestParam("skuId") Long skuId);
}
