package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.system.domain.vo.ConsoleDashboardVo;
import cn.org.starpivot.system.service.ConsoleDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "工作台")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ConsoleDashboardService consoleDashboardService;

    @Operation(summary = "获取工作台首页数据")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/console")
    public Result<ConsoleDashboardVo> console() {
        return Result.success(consoleDashboardService.getConsoleData());
    }
}
