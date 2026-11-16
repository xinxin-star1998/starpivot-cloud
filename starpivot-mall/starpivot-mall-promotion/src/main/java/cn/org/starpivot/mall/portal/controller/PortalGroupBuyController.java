package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.PortalMemberContext;
import cn.org.starpivot.mall.portal.domain.bo.PortalGroupBuyJoinBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalGroupBuyStartBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalGroupBuyActivityVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalGroupBuyDetailVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalGroupBuyRecordVo;
import cn.org.starpivot.mall.portal.service.PortalGroupBuyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C 端拼团控制器。
 */
@RestController
@RequestMapping("/portal/group-buy")
@RequiredArgsConstructor
@Validated
@Tag(name = "C端-拼团", description = "拼团活动、开团、参团")
public class PortalGroupBuyController {

    private final PortalGroupBuyService portalGroupBuyService;

    @Operation(summary = "拼团活动列表")
    @GetMapping
    public Result<List<PortalGroupBuyActivityVo>> list() {
        return Result.success(portalGroupBuyService.listActivities());
    }

    @Operation(summary = "拼团活动详情")
    @GetMapping("/{activityId}")
    public Result<PortalGroupBuyDetailVo> detail(@PathVariable("activityId") Long activityId) {
        return Result.success(portalGroupBuyService.detail(activityId));
    }

    @Operation(summary = "开团")
    @PostMapping("/start")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalGroupBuyRecordVo> start(@Valid @RequestBody PortalGroupBuyStartBo bo) {
        return Result.success(portalGroupBuyService.start(PortalMemberContext.requireMemberId(), bo));
    }

    @Operation(summary = "参团")
    @PostMapping("/join")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalGroupBuyRecordVo> join(@Valid @RequestBody PortalGroupBuyJoinBo bo) {
        return Result.success(portalGroupBuyService.join(PortalMemberContext.requireMemberId(), bo));
    }

    @Operation(summary = "我的拼团")
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<List<PortalGroupBuyRecordVo>> myGroups() {
        return Result.success(portalGroupBuyService.myGroups(PortalMemberContext.requireMemberId()));
    }
}
