package cn.org.starpivot.ai.controller;

import cn.org.starpivot.ai.domain.dto.AiKnowledgeBaseQueryDto;
import cn.org.starpivot.ai.domain.dto.AiKnowledgeBaseSaveDto;
import cn.org.starpivot.ai.domain.vo.AiKnowledgeBaseVo;
import cn.org.starpivot.ai.service.AiKnowledgeBaseService;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai/knowledge/base")
@RequiredArgsConstructor
@Tag(name = "AI-知识库", description = "知识库管理")
public class AiKnowledgeBaseController {

    private final AiKnowledgeBaseService aiKnowledgeBaseService;

    @PostMapping("/pageList")
    @PreAuthorize("hasAuthority('ai:knowledge:query')")
    @Operation(summary = "知识库分页")
    public Result<PageResponse<AiKnowledgeBaseVo>> pageList(@RequestBody AiKnowledgeBaseQueryDto query) {
        return Result.success(aiKnowledgeBaseService.pageList(query));
    }

    @GetMapping("/enabled")
    @PreAuthorize("hasAuthority('ai:knowledge:query')")
    @Operation(summary = "启用知识库列表")
    public Result<List<AiKnowledgeBaseVo>> enabled() {
        return Result.success(aiKnowledgeBaseService.listEnabled());
    }

    @GetMapping("/{kbId}")
    @PreAuthorize("hasAuthority('ai:knowledge:query')")
    @Operation(summary = "知识库详情")
    public Result<AiKnowledgeBaseVo> detail(@PathVariable Long kbId) {
        return Result.success(aiKnowledgeBaseService.getById(kbId));
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('ai:knowledge:edit')")
    @Operation(summary = "保存知识库")
    public Result<Long> save(@Valid @RequestBody AiKnowledgeBaseSaveDto dto) {
        return Result.success(aiKnowledgeBaseService.save(dto));
    }

    @DeleteMapping("/{kbId}")
    @PreAuthorize("hasAuthority('ai:knowledge:delete')")
    @Operation(summary = "删除知识库")
    public Result<Void> remove(@PathVariable Long kbId) {
        aiKnowledgeBaseService.remove(kbId);
        return Result.success();
    }
}
