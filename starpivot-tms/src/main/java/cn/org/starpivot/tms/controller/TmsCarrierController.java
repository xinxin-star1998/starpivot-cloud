package cn.org.starpivot.tms.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.tms.domain.dto.TmsCarrierQueryDto;
import cn.org.starpivot.tms.domain.dto.TmsCarrierSaveDto;
import cn.org.starpivot.tms.domain.vo.TmsCarrierVo;
import cn.org.starpivot.tms.service.TmsCarrierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tms/carrier")
@RequiredArgsConstructor
@Tag(name = "TMS-承运商", description = "承运商主数据")
public class TmsCarrierController {

    private final TmsCarrierService carrierService;

    @Operation(summary = "承运商分页")
    @PostMapping("/pageList")
    @PreAuthorize("hasAuthority('tms:carrier:query')")
    public Result<PageResponse<TmsCarrierVo>> pageList(@RequestBody TmsCarrierQueryDto query) {
        return Result.success(carrierService.pageList(query));
    }

    @Operation(summary = "启用承运商列表")
    @GetMapping("/enabled")
    @PreAuthorize("hasAuthority('tms:shipment:ship') or hasAuthority('mall:order:deliver') or hasAuthority('tms:carrier:query')")
    public Result<List<TmsCarrierVo>> listEnabled() {
        return Result.success(carrierService.listEnabled());
    }

    @Operation(summary = "保存承运商")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('tms:carrier:edit')")
    public Result<Long> save(@Valid @RequestBody TmsCarrierSaveDto dto) {
        return Result.success(carrierService.save(dto));
    }

    @Operation(summary = "删除承运商")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('tms:carrier:edit')")
    public Result<Void> remove(@PathVariable Long id) {
        carrierService.remove(id);
        return Result.success();
    }
}
