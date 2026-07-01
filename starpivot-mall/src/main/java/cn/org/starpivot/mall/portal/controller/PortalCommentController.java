package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.PageReqBo;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.PortalMemberContext;
import cn.org.starpivot.mall.portal.domain.bo.PortalCommentQueryBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalCommentReviewableBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalCommentSubmitBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCommentSummaryVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCommentVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalPendingReviewVo;
import cn.org.starpivot.mall.portal.service.PortalCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/portal/comment")
@RequiredArgsConstructor
@Validated
@Tag(name = "C端-评价", description = "商品评价")
public class PortalCommentController {

    private final PortalCommentService portalCommentService;

    @Operation(summary = "商品评价列表")
    @PostMapping("/commentPageList")
    public Result<PageResponse<PortalCommentVo>> pageList(@Valid @RequestBody PortalCommentQueryBo bo) {
        return Result.success(portalCommentService.pageBySpu(bo));
    }

    @Operation(summary = "我的评价")
    @PostMapping("/mineCommentPageList")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PageResponse<PortalCommentVo>> minePageList(@RequestBody PageReqBo pageReq) {
        return Result.success(portalCommentService.pageMine(PortalMemberContext.requireMemberId(), pageReq));
    }

    @Operation(summary = "批量查询可评价商品")
    @PostMapping("/reviewable-spu-ids")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<Map<String, List<Long>>> reviewableSpuIds(@Valid @RequestBody PortalCommentReviewableBo bo) {
        List<Long> ids = portalCommentService.filterReviewableSpuIds(
                PortalMemberContext.requireMemberId(), bo.getSpuIds());
        return Result.success(Map.of("reviewableSpuIds", ids));
    }

    @Operation(summary = "待评价列表")
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<List<PortalPendingReviewVo>> pendingList() {
        return Result.success(portalCommentService.listPendingReviews(PortalMemberContext.requireMemberId()));
    }

    @Operation(summary = "待评价数量")
    @GetMapping("/pending-count")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<Map<String, Integer>> pendingCount() {
        int count = portalCommentService.countPendingReviews(PortalMemberContext.requireMemberId());
        return Result.success(Map.of("count", count));
    }

    @Operation(summary = "商品评价摘要")
    @GetMapping("/summary/{spuId}")
    public Result<PortalCommentSummaryVo> summary(@PathVariable("spuId") Long spuId) {
        return Result.success(portalCommentService.getSummary(spuId));
    }

    @Operation(summary = "提交评价")
    @PostMapping("/submit")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> submit(@Valid @RequestBody PortalCommentSubmitBo bo) {
        portalCommentService.submit(PortalMemberContext.requireMemberId(), bo);
        return Result.success("评价成功");
    }

    @Operation(summary = "是否可评价")
    @GetMapping("/can-comment/{spuId}")
    public Result<Map<String, Boolean>> canComment(@PathVariable("spuId") Long spuId) {
        Long memberId = resolveMemberIdOrNull();
        boolean can = memberId != null && portalCommentService.canComment(memberId, spuId);
        return Result.success(Map.of("canComment", can));
    }

    private Long resolveMemberIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        try {
            return PortalMemberContext.requireMemberId();
        } catch (Exception ex) {
            return null;
        }
    }
}
