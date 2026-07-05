package cn.org.starpivot.api.tms;

import cn.org.starpivot.api.fallback.TmsInternalClientFallbackFactory;
import cn.org.starpivot.api.tms.dto.FreightCalculateRequest;
import cn.org.starpivot.api.tms.dto.FreightCalculateResult;
import cn.org.starpivot.api.tms.vo.ShipmentTrackingVo;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * TMS 内部 Feign 客户端。
 */
@FeignClient(
        name = "starpivot-tms",
        contextId = "tmsInternalClient",
        path = "/api/${starpivot.api.version:v1}",
        fallbackFactory = TmsInternalClientFallbackFactory.class)
public interface TmsInternalClient {

    @GetMapping("/internal/tms/shipment/tracking")
    Result<ShipmentTrackingVo> getTracking(
            @RequestParam("bizModule") String bizModule,
            @RequestParam("bizType") String bizType,
            @RequestParam("bizId") Long bizId);

    @PostMapping("/internal/tms/shipment/{shipmentId}/refresh-track")
    Result<ShipmentTrackingVo> refreshTrack(@PathVariable("shipmentId") Long shipmentId);

    @PostMapping("/internal/tms/freight/calculate")
    Result<FreightCalculateResult> calculateFreight(@RequestBody FreightCalculateRequest request);
}
