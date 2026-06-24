package cn.org.starpivot.mall.ums.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.ums.domain.bo.MemberLevelSaveBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberLevelVo;
import cn.org.starpivot.mall.ums.service.UmsMemberLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mall/member-level")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-会员等级")
public class UmsMemberLevelController {

    private final UmsMemberLevelService umsMemberLevelService;

    @Operation(summary = "会员等级列表")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('mall:member:level')")
    public Result<List<MemberLevelVo>> listAll() {
        return Result.success(umsMemberLevelService.listAll());
    }

    @Operation(summary = "会员等级详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:member:level')")
    public Result<MemberLevelVo> getById(@PathVariable("id") Long id) {
        return Result.success(umsMemberLevelService.getById(id));
    }

    @Log(title = "新增会员等级", businessType = BusinessType.INSERT)
    @Operation(summary = "新增会员等级")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:member:level:add')")
    public Result<?> add(@Valid @RequestBody MemberLevelSaveBo bo) {
        umsMemberLevelService.add(bo);
        return Result.success("新增成功");
    }

    @Log(title = "修改会员等级", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改会员等级")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:member:level:edit')")
    public Result<?> update(@Valid @RequestBody MemberLevelSaveBo bo) {
        umsMemberLevelService.update(bo);
        return Result.success("修改成功");
    }

    @Log(title = "删除会员等级", businessType = BusinessType.DELETE)
    @Operation(summary = "删除会员等级")
    @DeleteMapping("/remove")
    @PreAuthorize("hasAuthority('mall:member:level:delete')")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        List<Long> ids = validateIds(deleteRequest.getIds());
        umsMemberLevelService.removeByIds(ids);
        return Result.success("删除成功");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
