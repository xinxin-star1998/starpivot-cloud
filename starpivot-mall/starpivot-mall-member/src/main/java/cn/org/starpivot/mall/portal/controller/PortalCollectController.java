package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.PageReqBo;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.PortalMemberContext;
import cn.org.starpivot.mall.portal.domain.bo.PortalCollectAddBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalCollectSubjectAddBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCollectVo;
import cn.org.starpivot.mall.portal.service.PortalCollectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/portal/collect")
@RequiredArgsConstructor
@Validated
@Tag(name = "C端-收藏", description = "商品收藏")
public class PortalCollectController {

    private final PortalCollectService portalCollectService;

    @Operation(summary = "收藏列表")
    @PostMapping("/collectPageList")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PageResponse<PortalCollectVo>> pageList(@RequestBody PageReqBo pageReq) {
        return Result.success(portalCollectService.pageList(PortalMemberContext.requireMemberId(), pageReq));
    }

    @Operation(summary = "添加收藏")
    @PostMapping
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> add(@Valid @RequestBody PortalCollectAddBo bo) {
        portalCollectService.add(PortalMemberContext.requireMemberId(), bo);
        return Result.success("收藏成功");
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{spuId}")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> remove(@PathVariable("spuId") Long spuId) {
        portalCollectService.remove(PortalMemberContext.requireMemberId(), spuId);
        return Result.success("已取消收藏");
    }

    @Operation(summary = "是否已收藏")
    @GetMapping("/status/{spuId}")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<Map<String, Boolean>> status(@PathVariable("spuId") Long spuId) {
        boolean collected = portalCollectService.isCollected(PortalMemberContext.requireMemberId(), spuId);
        return Result.success(Map.of("collected", collected));
    }

    @Operation(summary = "收藏专题")
    @PostMapping("/subject")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> addSubject(@Valid @RequestBody PortalCollectSubjectAddBo bo) {
        portalCollectService.addSubject(PortalMemberContext.requireMemberId(), bo);
        return Result.success("收藏成功");
    }

    @Operation(summary = "取消收藏专题")
    @DeleteMapping("/subject/{subjectId}")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> removeSubject(@PathVariable("subjectId") Long subjectId) {
        portalCollectService.removeSubject(PortalMemberContext.requireMemberId(), subjectId);
        return Result.success("已取消收藏");
    }

    @Operation(summary = "是否已收藏专题")
    @GetMapping("/subject/status/{subjectId}")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<Map<String, Boolean>> subjectStatus(@PathVariable("subjectId") Long subjectId) {
        boolean collected = portalCollectService.isSubjectCollected(
                PortalMemberContext.requireMemberId(), subjectId);
        return Result.success(Map.of("collected", collected));
    }
}
