package cn.org.starpivot.mall.ums.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.mall.ums.domain.bo.MemberReqBo;
import cn.org.starpivot.mall.ums.domain.bo.MemberSaveBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberVo;
import cn.org.starpivot.mall.ums.service.UmsMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mall/member")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-会员")
public class UmsMemberController {

    private final UmsMemberService umsMemberService;

    @Operation(summary = "会员分页列表")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('mall:member:query')")
    public Result<PageResponse<MemberVo>> pageList(@RequestBody MemberReqBo reqBo) {
        return Result.success(umsMemberService.pageList(reqBo));
    }

    @Operation(summary = "会员详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:member:query')")
    public Result<MemberVo> getById(@PathVariable("id") Long id) {
        return Result.success(umsMemberService.getById(id));
    }

    @Log(title = "修改会员", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改会员")
    @PutMapping
    @PreAuthorize("hasAuthority('mall:member:edit')")
    public Result<?> update(@Valid @RequestBody MemberSaveBo bo) {
        umsMemberService.update(bo);
        return Result.success("修改成功");
    }
}
