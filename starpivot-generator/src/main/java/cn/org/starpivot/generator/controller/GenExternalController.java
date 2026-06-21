package cn.org.starpivot.generator.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.generator.domain.dto.external.*;
import cn.org.starpivot.generator.domain.entity.GenTable;
import cn.org.starpivot.generator.domain.entity.GenTableColumn;
import cn.org.starpivot.generator.domain.external.ExternalDbConnection;
import cn.org.starpivot.generator.service.GenExternalService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 外部库代码生成（会话级，不落 gen_table）
 */
@RestController
@RequestMapping("/tool/gen/external")
@RequiredArgsConstructor
public class GenExternalController {

    private final GenExternalService genExternalService;

    @PreAuthorize("hasAuthority('tool:external:query')")
    @PostMapping("/connect")
    public Result<ExternalConnectResultVO> connect(@Valid @RequestBody ExternalDbConnection connection) {
        return Result.success(genExternalService.connect(connection));
    }

    @PreAuthorize("hasAuthority('tool:external:query')")
    @DeleteMapping("/disconnect/{sessionId}")
    public Result<Void> disconnect(@PathVariable String sessionId) {
        genExternalService.disconnect(sessionId);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('tool:external:query')")
    @GetMapping("/session/{sessionId}")
    public Result<ExternalSessionStatusVO> sessionStatus(@PathVariable String sessionId) {
        return Result.success(genExternalService.getSessionStatus(sessionId));
    }

    @PreAuthorize("hasAuthority('tool:external:query')")
    @PostMapping("/tables")
    public Result<PageResponse<GenTable>> tables(@Valid @RequestBody ExternalTableQueryDTO query) {
        return Result.success(genExternalService.listTables(query));
    }

    @PreAuthorize("hasAuthority('tool:external:query')")
    @PostMapping("/columns")
    public Result<List<GenTableColumn>> columns(@Valid @RequestBody ExternalColumnsRequest request) {
        return Result.success(genExternalService.listColumns(request));
    }

    @PreAuthorize("hasAuthority('tool:external:query')")
    @PutMapping("/path-profile")
    public Result<Void> savePathProfile(@Valid @RequestBody ExternalPathProfileRequest request) {
        genExternalService.savePathProfile(request);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('tool:external:query')")
    @PutMapping("/draft")
    public Result<Void> saveDraft(@Valid @RequestBody ExternalDraftRequest request) {
        genExternalService.saveDraft(request);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('tool:external:query')")
    @PutMapping("/gen-config")
    public Result<Void> saveGenConfig(@Valid @RequestBody ExternalGenConfigRequest request) {
        genExternalService.saveGenConfig(request);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('tool:external:query')")
    @PutMapping("/template-dir")
    public Result<Void> saveTemplateDir(@Valid @RequestBody ExternalTemplateDirRequest request) {
        genExternalService.saveTemplateDir(request);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('tool:external:preview')")
    @PostMapping("/preview")
    public Result<Map<String, String>> preview(@Valid @RequestBody ExternalPreviewRequest request) {
        return Result.success(genExternalService.preview(request));
    }

    @Log(title = "外部库代码生成下载", businessType = BusinessType.GENCODE)
    @PreAuthorize("hasAuthority('tool:external:create')")
    @PostMapping("/download")
    public void download(@Valid @RequestBody ExternalDownloadRequest request, HttpServletResponse response)
            throws IOException {
        byte[] data = genExternalService.download(request);
        String filename = "codegen_external_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".zip";
        response.reset();
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.addHeader("Content-Length", String.valueOf(data.length));
        response.setContentType("application/octet-stream; charset=UTF-8");
        IOUtils.write(data, response.getOutputStream());
    }

    @Log(title = "外部库代码写盘生成", businessType = BusinessType.GENCODE)
    @PreAuthorize("hasAuthority('tool:external:create')")
    @PostMapping("/write")
    public Result<ExternalWriteResultVO> write(@Valid @RequestBody ExternalWriteRequest request) {
        return Result.success(genExternalService.writeToDisk(request));
    }

    @PreAuthorize("hasAuthority('tool:external:preview')")
    @PostMapping("/write-diff")
    public Result<ExternalWriteDiffResultVO> writeDiff(@Valid @RequestBody ExternalWriteDiffRequest request) {
        return Result.success(genExternalService.previewWriteDiff(request));
    }

    @PreAuthorize("hasAuthority('tool:external:preview')")
    @PostMapping("/write-diff/file")
    public Result<ExternalWriteDiffItemVO> writeDiffFile(@Valid @RequestBody ExternalWriteDiffFileRequest request) {
        return Result.success(genExternalService.previewWriteDiffFile(request));
    }

    @Log(title = "外部库导入代码生成", businessType = BusinessType.IMPORT)
    @PreAuthorize("hasAuthority('tool:external:create')")
    @PostMapping("/import-gen-table")
    public Result<ExternalImportResultVO> importGenTable(@Valid @RequestBody ExternalImportRequest request) {
        return Result.success(genExternalService.importToGenTable(request));
    }

    @PreAuthorize("hasAuthority('tool:external:query')")
    @GetMapping("/templates")
    public Result<Map<String, Object>> templates() {
        return Result.success(genExternalService.listTemplates());
    }

    @PreAuthorize("hasAuthority('tool:external:query')")
    @GetMapping("/write-dirs")
    public Result<ExternalWriteDirListVO> writeDirs(@RequestParam(required = false) String path) {
        return Result.success(genExternalService.listWriteDirs(path));
    }

    @Log(title = "外部库代码写盘回退", businessType = BusinessType.GENCODE)
    @PreAuthorize("hasAuthority('tool:external:create')")
    @PostMapping("/write-rollback")
    public Result<ExternalWriteRollbackResultVO> writeRollback(@Valid @RequestBody ExternalWriteRollbackRequest request) {
        return Result.success(genExternalService.rollbackWrite(request));
    }

    @PreAuthorize("hasAuthority('tool:external:query')")
    @GetMapping("/capabilities")
    public Result<Map<String, Object>> capabilities() {
        return Result.success(genExternalService.listCapabilities());
    }
}

