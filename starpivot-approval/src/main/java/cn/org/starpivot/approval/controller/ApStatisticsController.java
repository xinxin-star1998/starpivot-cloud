package cn.org.starpivot.approval.controller;

import cn.org.starpivot.approval.domain.vo.ApStatisticsVo;
import cn.org.starpivot.approval.service.ApStatisticsService;
import cn.org.starpivot.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/approval/statistics")
@RequiredArgsConstructor
@Tag(name = "审批统计", description = "审批数据看板")
public class ApStatisticsController {

    private final ApStatisticsService statisticsService;

    @Operation(summary = "看板汇总")
    @PreAuthorize("hasAuthority('approval:statistics:query')")
    @GetMapping("/dashboard")
    public Result<ApStatisticsVo> dashboard(@RequestParam(required = false) String bizModule) {
        return Result.success(statisticsService.dashboard(bizModule));
    }
}
