package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.PortalMemberContext;
import cn.org.starpivot.mall.portal.domain.bo.PortalBargainHelpBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalBargainOrderBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalBargainStartBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalBargainActivityVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalBargainDetailVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalBargainRecordVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderSubmitVo;
import cn.org.starpivot.mall.portal.service.PortalBargainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C 端砍价控制器。
 */
@RestController
@RequestMapping("/portal/bargain")
@RequiredArgsConstructor
@Validated
@Tag(name = "C端-砍价", description = "砍价活动、发起、助力、下单")
public class PortalBargainController {

    private final PortalBargainService portalBargainService;

    @Operation(summary = "砍价活动列表")
    @GetMapping
    public Result<List<PortalBargainActivityVo>> list() {
        return Result.success(portalBargainService.listActivities());
    }

    @Operation(summary = "砍价活动详情")
    @GetMapping("/{activityId}")
    public Result<PortalBargainDetailVo> detail(@PathVariable("activityId") Long activityId) {
        return Result.success(portalBargainService.detail(SecurityContextUtils.getUserId(), activityId));
    }

    @Operation(summary = "砍价记录详情（好友助力）")
    @GetMapping("/record/{recordId}")
    public Result<PortalBargainRecordVo> recordDetail(@PathVariable("recordId") Long recordId) {
        return Result.success(portalBargainService.recordDetail(recordId));
    }

    @Operation(summary = "发起砍价")
    @PostMapping("/start")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalBargainRecordVo> start(@Valid @RequestBody PortalBargainStartBo bo) {
        return Result.success(portalBargainService.start(PortalMemberContext.requireMemberId(), bo));
    }

    @Operation(summary = "好友助力")
    @PostMapping("/help")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalBargainRecordVo> help(@Valid @RequestBody PortalBargainHelpBo bo) {
        return Result.success(portalBargainService.help(PortalMemberContext.requireMemberId(), bo));
    }

    @Operation(summary = "砍价下单")
    @PostMapping("/order")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalBargainRecordVo> order(@Valid @RequestBody PortalBargainOrderBo bo) {
        return Result.success(portalBargainService.order(PortalMemberContext.requireMemberId(), bo));
    }

    @Operation(summary = "我的砍价")
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<List<PortalBargainRecordVo>> myBargains() {
        return Result.success(portalBargainService.myBargains(PortalMemberContext.requireMemberId()));
    }
}