package cn.org.starpivot.tms.controller.internal;

import cn.org.starpivot.api.tms.vo.ShipmentTrackingVo;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.tms.service.TmsShipmentService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Hidden
@RestController
@RequiredArgsConstructor
public class TmsShipmentInternalController {

    private final TmsShipmentService shipmentService;

    @GetMapping("/internal/tms/shipment/tracking")
    public Result<ShipmentTrackingVo> getTracking(
            @RequestParam("bizModule") String bizModule,
            @RequestParam("bizType") String bizType,
            @RequestParam("bizId") Long bizId) {
        ShipmentTrackingVo tracking = shipmentService.getTracking(bizModule, bizType, bizId);
        if (tracking == null) {
            return Result.notFound("运单不存在");
        }
        return Result.success(tracking);
    }

    @PostMapping("/internal/tms/shipment/{shipmentId}/refresh-track")
    public Result<ShipmentTrackingVo> refreshTrack(@PathVariable("shipmentId") Long shipmentId) {
        return Result.success(shipmentService.refreshTrack(shipmentId));
    }
}
