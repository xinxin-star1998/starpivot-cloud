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

/**
 * 工作台控制器。
 * <p>
 * 为已登录用户提供控制台首页聚合数据。
 * </p>
 * <ul>
 *   <li>{@link Tag} — OpenAPI 分组「工作台」</li>
 *   <li>{@link RestController} — REST 控制器</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /dashboard}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入 {@link ConsoleDashboardService}</li>
 * </ul>
 */
@Tag(name = "工作台")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ConsoleDashboardService consoleDashboardService;

    /**
     * 获取工作台首页统计数据。
     *
     * @return 控制台仪表盘视图对象
     */
    @Operation(summary = "获取工作台首页数据")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/console")
    public Result<ConsoleDashboardVo> console() {
        return Result.success(consoleDashboardService.getConsoleData());
    }
}
