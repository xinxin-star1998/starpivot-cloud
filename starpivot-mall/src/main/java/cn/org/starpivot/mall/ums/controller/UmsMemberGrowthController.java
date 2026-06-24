package cn.org.starpivot.mall.ums.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.ums.domain.bo.MemberGrowthReqBo;
import cn.org.starpivot.mall.ums.domain.vo.GrowthChangeHistoryVo;
import cn.org.starpivot.mall.ums.domain.vo.IntegrationChangeHistoryVo;
import cn.org.starpivot.mall.ums.service.UmsMemberGrowthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mall/member-growth")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-会员积分成长")
public class UmsMemberGrowthController {

    private final UmsMemberGrowthService umsMemberGrowthService;

    @Operation(summary = "积分变动记录分页列表")
    @PostMapping("/integration/list")
    @PreAuthorize("hasAuthority('mall:member:growth')")
    public Result<PageResponse<IntegrationChangeHistoryVo>> integrationPageList(
            @RequestBody MemberGrowthReqBo reqBo) {
        return Result.success(umsMemberGrowthService.integrationPageList(reqBo));
    }

    @Operation(summary = "成长值变动记录分页列表")
    @PostMapping("/growth/list")
    @PreAuthorize("hasAuthority('mall:member:growth')")
    public Result<PageResponse<GrowthChangeHistoryVo>> growthPageList(@RequestBody MemberGrowthReqBo reqBo) {
        return Result.success(umsMemberGrowthService.growthPageList(reqBo));
    }
}
