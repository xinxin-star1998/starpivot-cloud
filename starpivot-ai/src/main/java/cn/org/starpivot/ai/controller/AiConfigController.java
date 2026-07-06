package cn.org.starpivot.ai.controller;

import cn.org.starpivot.ai.domain.dto.AiConfigQueryDto;
import cn.org.starpivot.ai.domain.dto.AiConfigSaveDto;
import cn.org.starpivot.ai.domain.vo.AiConfigVo;
import cn.org.starpivot.ai.service.AiConfigService;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai/config")
@RequiredArgsConstructor
@Tag(name = "AI-基础配置", description = "AI 助手基础配置管理")
public class AiConfigController {

    private final AiConfigService aiConfigService;

    @Operation(summary = "配置分页列表")
    @PostMapping("/pageList")
    @PreAuthorize("hasAuthority('ai:config:query')")
    public Result<PageResponse<AiConfigVo>> pageList(@RequestBody AiConfigQueryDto query) {
        return Result.success(aiConfigService.pageList(query));
    }

    @Operation(summary = "配置详情")
    @GetMapping("/{configId}")
    @PreAuthorize("hasAuthority('ai:config:query')")
    public Result<AiConfigVo> detail(@PathVariable Long configId) {
        return Result.success(aiConfigService.getById(configId));
    }

    @Operation(summary = "保存配置")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('ai:config:edit')")
    public Result<Long> save(@Valid @RequestBody AiConfigSaveDto dto) {
        return Result.success(aiConfigService.save(dto));
    }

    @Operation(summary = "删除配置")
    @DeleteMapping("/{configId}")
    @PreAuthorize("hasAuthority('ai:config:delete')")
    public Result<Void> remove(@PathVariable Long configId) {
        aiConfigService.remove(configId);
        return Result.success();
    }

    @Operation(summary = "设为默认配置")
    @PutMapping("/{configId}/default")
    @PreAuthorize("hasAuthority('ai:config:edit')")
    public Result<Void> setDefault(@PathVariable Long configId) {
        aiConfigService.setDefault(configId);
        return Result.success();
    }
}
