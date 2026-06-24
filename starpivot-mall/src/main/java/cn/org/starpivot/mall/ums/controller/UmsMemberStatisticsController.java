package cn.org.starpivot.mall.ums.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.mall.ums.domain.bo.MemberStatisticsReqBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberStatisticsVo;
import cn.org.starpivot.mall.ums.service.UmsMemberStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/mall/member-statistics")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-会员统计")
public class UmsMemberStatisticsController {

    private final UmsMemberStatisticsService umsMemberStatisticsService;

    @Operation(summary = "会员统计分页列表")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('mall:member:statistics')")
    public Result<PageResponse<MemberStatisticsVo>> pageList(@RequestBody MemberStatisticsReqBo reqBo) {
        return Result.success(umsMemberStatisticsService.pageList(reqBo));
    }

    @Operation(summary = "按会员ID查询统计")
    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAuthority('mall:member:statistics')")
    public Result<MemberStatisticsVo> getByMemberId(@PathVariable("memberId") Long memberId) {
        return Result.success(umsMemberStatisticsService.getByMemberId(memberId));
    }

    @Log(title = "刷新会员统计", businessType = BusinessType.UPDATE)
    @Operation(summary = "刷新会员统计（从业务表聚合写入快照）")
    @PutMapping("/refresh/{memberId}")
    @PreAuthorize("hasAuthority('mall:member:statistics')")
    public Result<?> refresh(@PathVariable("memberId") Long memberId) {
        umsMemberStatisticsService.refresh(memberId);
        return Result.success("刷新成功");
    }
}
