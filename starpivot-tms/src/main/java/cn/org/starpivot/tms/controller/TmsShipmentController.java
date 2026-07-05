package cn.org.starpivot.tms.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.tms.domain.dto.TmsShipmentQueryDto;
import cn.org.starpivot.tms.domain.dto.TmsShipmentShipDto;
import cn.org.starpivot.tms.domain.vo.TmsShipmentVo;
import cn.org.starpivot.tms.service.TmsShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tms/shipment")
@RequiredArgsConstructor
@Tag(name = "TMS-运单", description = "运单发货与轨迹")
public class TmsShipmentController {

    private final TmsShipmentService shipmentService;

    @Operation(summary = "运单分页")
    @PostMapping("/pageList")
    @PreAuthorize("hasAuthority('tms:shipment:query')")
    public Result<PageResponse<TmsShipmentVo>> pageList(@RequestBody TmsShipmentQueryDto query) {
        return Result.success(shipmentService.pageList(query));
    }

    @Operation(summary = "运单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('tms:shipment:query')")
    public Result<TmsShipmentVo> detail(@PathVariable Long id) {
        return Result.success(shipmentService.getDetail(id));
    }

    @Operation(summary = "订单发货")
    @PostMapping("/ship")
    @PreAuthorize("hasAuthority('tms:shipment:ship') or hasAuthority('mall:order:deliver')")
    public Result<Long> ship(@Valid @RequestBody TmsShipmentShipDto dto) {
        return Result.success(shipmentService.ship(dto));
    }

    @Operation(summary = "刷新轨迹")
    @PostMapping("/{id}/refresh-track")
    @PreAuthorize("hasAuthority('tms:shipment:track')")
    public Result<TmsShipmentVo> refreshTrack(@PathVariable Long id) {
        shipmentService.refreshTrack(id);
        return Result.success(shipmentService.getDetail(id));
    }
}
