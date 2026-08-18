package cn.org.starpivot.ai.controller;

import cn.org.starpivot.ai.domain.dto.AiProviderQueryDto;
import cn.org.starpivot.ai.domain.dto.AiProviderSaveDto;
import cn.org.starpivot.ai.domain.vo.AiModelVo;
import cn.org.starpivot.ai.domain.vo.AiProviderPresetVo;
import cn.org.starpivot.ai.domain.vo.AiProviderVo;
import cn.org.starpivot.ai.service.AiProviderService;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai/provider")
@RequiredArgsConstructor
@Tag(name = "AI-模型供应商", description = "DeepSeek / Kimi / 百炼等 API 供应商配置")
public class AiProviderController {

    private final AiProviderService aiProviderService;

    @Operation(summary = "供应商分页")
    @PostMapping("/pageList")
    @PreAuthorize("hasAuthority('ai:provider:query')")
    public Result<PageResponse<AiProviderVo>> pageList(@RequestBody AiProviderQueryDto query) {
        return Result.success(aiProviderService.pageList(query));
    }

    @Operation(summary = "供应商详情")
    @GetMapping("/{providerId}")
    @PreAuthorize("hasAuthority('ai:provider:query')")
    public Result<AiProviderVo> detail(@PathVariable Long providerId) {
        return Result.success(aiProviderService.getById(providerId));
    }

    @Operation(summary = "预设模板")
    @GetMapping("/presets")
    @PreAuthorize("hasAuthority('ai:provider:query')")
    public Result<List<AiProviderPresetVo>> presets() {
        return Result.success(aiProviderService.presets());
    }

    @Operation(summary = "可用对话模型")
    @GetMapping("/chat-models")
    @PreAuthorize("hasAnyAuthority('ai:provider:query','ai:config:query','ai:chat:use')")
    public Result<List<AiModelVo>> chatModels() {
        return Result.success(aiProviderService.listChatModels());
    }

    @Operation(summary = "保存供应商")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('ai:provider:edit')")
    public Result<Long> save(@Valid @RequestBody AiProviderSaveDto dto) {
        return Result.success(aiProviderService.save(dto));
    }

    @Operation(summary = "删除供应商")
    @DeleteMapping("/{providerId}")
    @PreAuthorize("hasAuthority('ai:provider:delete')")
    public Result<Void> remove(@PathVariable Long providerId) {
        aiProviderService.remove(providerId);
        return Result.success();
    }

    @Operation(summary = "设为默认供应商")
    @PutMapping("/{providerId}/default")
    @PreAuthorize("hasAuthority('ai:provider:edit')")
    public Result<Void> setDefault(
            @PathVariable Long providerId, @RequestParam(defaultValue = "chat") String kind) {
        aiProviderService.setDefault(providerId, kind);
        return Result.success();
    }

    @Operation(summary = "测试连通性")
    @PostMapping("/{providerId}/test")
    @PreAuthorize("hasAuthority('ai:provider:edit')")
    public Result<String> test(
            @PathVariable Long providerId, @RequestParam(defaultValue = "chat") String kind) {
        return Result.success(aiProviderService.testConnection(providerId, kind));
    }
}
