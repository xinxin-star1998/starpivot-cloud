package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.system.domain.bo.SysConfigVO;
import cn.org.starpivot.system.domain.dto.SysConfigDTO;
import cn.org.starpivot.system.domain.dto.SysConfigQueryDTO;
import cn.org.starpivot.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
@Tag(name = "参数配置")
public class SysConfigController {

    private final SysConfigService sysConfigService;

    @PreAuthorize("hasAuthority('system:config:query')")
    @PostMapping("/list")
    public Result<PageResponse<SysConfigVO>> list(@RequestBody SysConfigQueryDTO queryDTO) {
        return Result.success(sysConfigService.selectSysConfigPage(queryDTO));
    }

    @PreAuthorize("hasAuthority('system:config:query')")
    @GetMapping("/{configId}")
    public Result<SysConfigVO> getInfo(@PathVariable Long configId) {
        return Result.success(sysConfigService.selectSysConfigByConfigId(configId));
    }

    @Log(title = "新增参数", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('system:config:add')")
    @PostMapping
    public Result<?> add(@Valid @RequestBody SysConfigDTO sysConfigDTO) {
        boolean success = sysConfigService.insertSysConfig(sysConfigDTO);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    @Log(title = "修改参数", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('system:config:edit')")
    @PutMapping
    public Result<?> edit(@Valid @RequestBody SysConfigDTO sysConfigDTO) {
        boolean success = sysConfigService.updateSysConfig(sysConfigDTO);
        return success ? Result.success("修改成功") : Result.error("修改失败");
    }

    @Log(title = "删除参数", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('system:config:delete')")
    @DeleteMapping("/delete")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest.getIds() == null || deleteRequest.getIds().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        boolean success = sysConfigService.deleteSysConfigByConfigIds(
                deleteRequest.getIds().toArray(Long[]::new));
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
