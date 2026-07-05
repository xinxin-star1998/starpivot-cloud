package cn.org.starpivot.mall.ware.controller.internal;

import cn.org.starpivot.api.mall.ware.dto.*;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.ware.internal.WareInternalService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Hidden
@RestController
@RequestMapping("/internal/ware")
@RequiredArgsConstructor
public class WareInternalController {

    private final WareInternalService wareInternalService;

    @PostMapping("/sku/add-stock")
    public Result<Void> addStock(@Valid @RequestBody WareSkuAddStockRequest request) {
        wareInternalService.addStock(request.getSkuId(), request.getWareId(), request.getQuantity());
        return Result.success();
    }

    @PostMapping("/stock/wms-available")
    public Result<Map<Long, Integer>> getWmsAvailableMap(@RequestBody Collection<Long> skuIds) {
        return Result.success(wareInternalService.getWmsAvailableMap(skuIds));
    }

    @PostMapping("/stock/deduct")
    public Result<List<StockDeductionLineDto>> deductStock(@Valid @RequestBody StockDeductRequest request) {
        return Result.success(wareInternalService.deductStock(request));
    }

    @PostMapping("/order-task/finished-for-paid")
    public Result<Long> createFinishedRecordForPaidOrder(@Valid @RequestBody OrderTaskFinishedRequest request) {
        return Result.success(wareInternalService.createFinishedRecordForPaidOrder(
                request.getOrderId(), request.getDeductions()));
    }

    @PostMapping("/stock/restore-for-order/{orderId}")
    public Result<Map<Long, Integer>> restoreStockForOrder(@PathVariable("orderId") Long orderId) {
        return Result.success(wareInternalService.restoreStockForOrder(orderId));
    }

    @PostMapping("/stock/inbound-for-return")
    public Result<Long> inboundForReturn(@Valid @RequestBody InboundForReturnRequest request) {
        return Result.success(wareInternalService.inboundForReturn(
                request.getOrderId(), request.getSkuId(), request.getQuantity()));
    }

    @GetMapping("/outbound-ware")
    public Result<Long> findOutboundWareId(
            @RequestParam("orderId") Long orderId,
            @RequestParam("skuId") Long skuId) {
        return Result.success(wareInternalService.findOutboundWareId(orderId, skuId));
    }

    @PutMapping("/order-task/tracking")
    public Result<Void> updateTrackingByOrderId(@Valid @RequestBody OrderTaskTrackingRequest request) {
        wareInternalService.updateTrackingByOrderId(request.getOrderId(), request.getTrackingNo());
        return Result.success();
    }
}
