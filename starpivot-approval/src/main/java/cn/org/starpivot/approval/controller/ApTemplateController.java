package cn.org.starpivot.approval.controller;

import cn.org.starpivot.approval.domain.dto.ApTemplateBindQueryDto;
import cn.org.starpivot.approval.domain.dto.ApTemplateBindSaveDto;
import cn.org.starpivot.approval.domain.dto.ApTemplateQueryDto;
import cn.org.starpivot.approval.domain.dto.ApTemplateSaveDto;
import cn.org.starpivot.approval.domain.entity.ApTemplate;
import cn.org.starpivot.approval.domain.entity.ApTemplateBind;
import cn.org.starpivot.approval.domain.vo.ApTemplateDetailVo;
import cn.org.starpivot.approval.service.ApTemplateService;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.security.SecurityContextUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/approval/template")
@RequiredArgsConstructor
@Tag(name = "审批模板", description = "模板维护、发布、业务绑定")
public class ApTemplateController {

    private final ApTemplateService templateService;

    @PreAuthorize("hasAuthority('approval:template:query')")
    @PostMapping("/templatePageList")
    public Result<PageResponse<ApTemplate>> list(@RequestBody ApTemplateQueryDto query) {
        return Result.success(templateService.pageList(query));
    }

    @PreAuthorize("hasAuthority('approval:template:query')")
    @GetMapping("/{id}")
    public Result<ApTemplateDetailVo> detail(@PathVariable Long id) {
        return Result.success(templateService.getDetail(id));
    }

    @PreAuthorize("hasAuthority('approval:template:edit')")
    @PostMapping("/save")
    public Result<Long> save(@Valid @RequestBody ApTemplateSaveDto dto) {
        return Result.success(templateService.save(dto, SecurityContextUtils.getUsername()));
    }

    @PreAuthorize("hasAuthority('approval:template:publish')")
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        templateService.publish(id, SecurityContextUtils.getUsername());
        return Result.success();
    }

    @PreAuthorize("hasAuthority('approval:template:edit')")
    @PostMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        templateService.disable(id, SecurityContextUtils.getUsername());
        return Result.success();
    }

    @PreAuthorize("hasAuthority('approval:bind:edit')")
    @PostMapping("/templateBindPageList")
    public Result<PageResponse<ApTemplateBind>> bindList(@RequestBody ApTemplateBindQueryDto query) {
        return Result.success(templateService.pageBindList(query));
    }

    @PreAuthorize("hasAuthority('approval:bind:edit')")
    @PostMapping("/bind/save")
    public Result<Void> saveBind(@Valid @RequestBody ApTemplateBindSaveDto dto) {
        templateService.saveBind(dto);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('approval:bind:edit')")
    @DeleteMapping("/removeTemplateBind")
    public Result<Void> removeBind(@RequestBody DeleteRequest request) {
        templateService.deleteBind(request.getIds());
        return Result.success();
    }
}
