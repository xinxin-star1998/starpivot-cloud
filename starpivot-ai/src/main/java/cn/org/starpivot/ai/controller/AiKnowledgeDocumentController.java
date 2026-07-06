package cn.org.starpivot.ai.controller;

import cn.org.starpivot.ai.domain.dto.AiKnowledgeDocumentQueryDto;
import cn.org.starpivot.ai.domain.dto.AiKnowledgeDocumentSaveDto;
import cn.org.starpivot.ai.domain.vo.AiKnowledgeDocumentVo;
import cn.org.starpivot.ai.service.AiKnowledgeDocumentService;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai/knowledge/document")
@RequiredArgsConstructor
@Tag(name = "AI-知识库文档", description = "知识库文档管理")
public class AiKnowledgeDocumentController {

    private final AiKnowledgeDocumentService aiKnowledgeDocumentService;

    @PostMapping("/pageList")
    @PreAuthorize("hasAuthority('ai:knowledge:query')")
    @Operation(summary = "文档分页")
    public Result<PageResponse<AiKnowledgeDocumentVo>> pageList(@Valid @RequestBody AiKnowledgeDocumentQueryDto query) {
        return Result.success(aiKnowledgeDocumentService.pageList(query));
    }

    @GetMapping("/{docId}")
    @PreAuthorize("hasAuthority('ai:knowledge:query')")
    @Operation(summary = "文档详情")
    public Result<AiKnowledgeDocumentVo> detail(@PathVariable Long docId) {
        return Result.success(aiKnowledgeDocumentService.getById(docId));
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('ai:knowledge:edit')")
    @Operation(summary = "保存文档")
    public Result<Long> save(@Valid @RequestBody AiKnowledgeDocumentSaveDto dto) {
        return Result.success(aiKnowledgeDocumentService.save(dto));
    }

    @DeleteMapping("/{docId}")
    @PreAuthorize("hasAuthority('ai:knowledge:delete')")
    @Operation(summary = "删除文档")
    public Result<Void> remove(@PathVariable Long docId) {
        aiKnowledgeDocumentService.remove(docId);
        return Result.success();
    }

    @PostMapping("/{docId}/reindex")
    @PreAuthorize("hasAuthority('ai:knowledge:edit')")
    @Operation(summary = "重建文档分块")
    public Result<Void> reindex(@PathVariable Long docId) {
        aiKnowledgeDocumentService.reindex(docId);
        return Result.success();
    }

    @PostMapping(value = "/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ai:knowledge:edit')")
    @Operation(summary = "上传文档文件")
    public Result<Long> upload(
            @RequestParam Long kbId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return Result.success(aiKnowledgeDocumentService.uploadFile(kbId, file));
    }
}
