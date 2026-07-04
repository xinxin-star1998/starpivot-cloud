package cn.org.starpivot.file.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.file.domain.bo.SysFileVO;
import cn.org.starpivot.file.domain.dto.*;
import cn.org.starpivot.file.service.ISysFileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文件中心文件接口。
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class SysFileController {

    private final ISysFileService sysFileService;

    @PreAuthorize("hasAuthority('file:resource:query')")
    @PostMapping("/filePageList")
    public Result<PageResponse<SysFileVO>> pageList(@Valid @RequestBody SysFileQueryDTO queryDTO) {
        return Result.success(sysFileService.pageList(queryDTO));
    }

    @Log(title = "上传文件", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('file:resource:add')")
    @PostMapping("/upload")
    public Result<SysFileVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folderId") Long folderId,
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) String bizId,
            @RequestParam(required = false) String remark) {
        SysFileUploadDTO uploadDTO = new SysFileUploadDTO();
        uploadDTO.setBizType(bizType);
        uploadDTO.setBizId(bizId);
        uploadDTO.setRemark(remark);
        return Result.success(sysFileService.upload(file, folderId, uploadDTO));
    }

    @PreAuthorize("hasAuthority('file:resource:query')")
    @GetMapping("/{fileId}")
    public Result<SysFileVO> detail(@PathVariable Long fileId) {
        return Result.success(sysFileService.getDetail(fileId));
    }

    @PreAuthorize("hasAuthority('file:resource:query')")
    @GetMapping("/preview-url/{fileId}")
    public Result<Map<String, String>> previewUrl(@PathVariable Long fileId) {
        return Result.success(sysFileService.previewUrl(fileId));
    }

    @Log(title = "删除文件", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('file:resource:delete')")
    @DeleteMapping("/removeFile")
    public Result<Void> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        sysFileService.logicDelete(ids);
        return Result.success();
    }

    @Log(title = "恢复文件", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('file:resource:restore')")
    @PutMapping("/restore")
    public Result<Void> restore(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        sysFileService.restore(ids);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('file:resource:query')")
    @PostMapping("/recycleFilePageList")
    public Result<PageResponse<SysFileVO>> recycleList(@Valid @RequestBody SysFileRecycleQueryDTO queryDTO) {
        return Result.success(sysFileService.recyclePage(queryDTO));
    }

    @Log(title = "永久删除文件", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('file:resource:purge')")
    @DeleteMapping("/recycle/purge")
    public Result<Void> permanentDelete(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        sysFileService.permanentDelete(ids);
        return Result.success();
    }

    @Log(title = "迁移文件", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('file:resource:move')")
    @PutMapping("/move")
    public Result<Void> move(@Valid @RequestBody SysFileMoveDTO moveDTO) {
        sysFileService.moveToFolder(moveDTO.getIds(), moveDTO.getTargetFolderId());
        return Result.success();
    }

    @Log(title = "重命名文件", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('file:resource:edit')")
    @PutMapping("/rename")
    public Result<Void> rename(@Valid @RequestBody SysFileRenameDTO renameDTO) {
        sysFileService.rename(renameDTO.getFileId(), renameDTO.getFileName());
        return Result.success();
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "ID列表不能为空");
        }
        return ids;
    }
}
