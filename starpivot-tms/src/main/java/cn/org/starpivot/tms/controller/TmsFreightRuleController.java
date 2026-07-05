package cn.org.starpivot.tms.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.tms.domain.dto.TmsFreightRuleQueryDto;
import cn.org.starpivot.tms.domain.dto.TmsFreightRuleSaveDto;
import cn.org.starpivot.tms.domain.vo.TmsFreightRuleVo;
import cn.org.starpivot.tms.service.TmsFreightRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tms/freight")
@RequiredArgsConstructor
@Tag(name = "TMS-运费规则", description = "运费规则配置")
public class TmsFreightRuleController {

    private final TmsFreightRuleService freightRuleService;

    @Operation(summary = "运费规则分页")
    @PostMapping("/pageList")
    @PreAuthorize("hasAuthority('tms:freight:query')")
    public Result<PageResponse<TmsFreightRuleVo>> pageList(@RequestBody TmsFreightRuleQueryDto query) {
        return Result.success(freightRuleService.pageList(query));
    }

    @Operation(summary = "保存运费规则")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('tms:freight:edit')")
    public Result<Long> save(@Valid @RequestBody TmsFreightRuleSaveDto dto) {
        return Result.success(freightRuleService.save(dto));
    }

    @Operation(summary = "删除运费规则")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('tms:freight:edit')")
    public Result<Void> remove(@PathVariable Long id) {
        freightRuleService.remove(id);
        return Result.success();
    }
}
