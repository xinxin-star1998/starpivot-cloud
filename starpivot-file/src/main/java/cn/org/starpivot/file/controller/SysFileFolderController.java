package cn.org.starpivot.file.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.file.domain.bo.FileCategoryNodeVO;
import cn.org.starpivot.file.domain.dto.SysFileFolderDTO;
import cn.org.starpivot.file.service.ISysFileFolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件中心文件夹接口。
 */
@RestController
@RequestMapping("/file/folder")
@RequiredArgsConstructor
public class SysFileFolderController {

    private final ISysFileFolderService sysFileFolderService;

    @PreAuthorize("hasAuthority('file:folder:query')")
    @GetMapping("/tree")
    public Result<List<FileCategoryNodeVO>> tree(@RequestParam(required = false) String category) {
        return Result.success(sysFileFolderService.listTree(category));
    }

    @Log(title = "新建文件夹", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('file:folder:add')")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody SysFileFolderDTO dto) {
        return Result.success(sysFileFolderService.create(dto));
    }

    @Log(title = "编辑文件夹", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('file:folder:edit')")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody SysFileFolderDTO dto) {
        sysFileFolderService.update(dto);
        return Result.success();
    }

    @Log(title = "删除文件夹", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('file:folder:delete')")
    @DeleteMapping("/{folderId}")
    public Result<Void> delete(@PathVariable Long folderId) {
        sysFileFolderService.delete(folderId);
        return Result.success();
    }
}
