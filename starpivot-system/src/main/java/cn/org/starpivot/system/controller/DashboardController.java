package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.system.domain.vo.ConsoleDashboardVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "工作台")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Operation(summary = "获取工作台首页数据")
    @GetMapping("/console")
    public Result<ConsoleDashboardVo> console() {
        return Result.success(buildConsoleData());
    }

    private ConsoleDashboardVo buildConsoleData() {
        return ConsoleDashboardVo.builder()
                .cardList(List.of(
                        ConsoleDashboardVo.CardItem.builder()
                                .des("总访问次数").icon("ri:pie-chart-line").num(9120L).change("+20%")
                                .build(),
                        ConsoleDashboardVo.CardItem.builder()
                                .des("在线访客数").icon("ri:group-line").num(182L).change("+10%")
                                .build(),
                        ConsoleDashboardVo.CardItem.builder()
                                .des("点击量").icon("ri:fire-line").num(9520L).change("-12%")
                                .build(),
                        ConsoleDashboardVo.CardItem.builder()
                                .des("新用户").icon("ri:progress-2-line").num(156L).change("+30%")
                                .build()
                ))
                .visitTrend(ConsoleDashboardVo.TrendData.builder()
                        .xAxisData(List.of("1月", "2月", "3月", "4月", "5月", "6月", "7月"))
                        .data(List.of(50L, 25L, 40L, 20L, 70L, 35L, 65L))
                        .build())
                .userTrend(ConsoleDashboardVo.TrendData.builder()
                        .xAxisData(List.of("周一", "周二", "周三", "周四", "周五", "周六", "周日"))
                        .data(List.of(120L, 132L, 101L, 134L, 90L, 230L, 210L))
                        .build())
                .todoList(List.of(
                        ConsoleDashboardVo.TodoItem.builder()
                                .username("admin").date("2026-06-18").complete(false).build(),
                        ConsoleDashboardVo.TodoItem.builder()
                                .username("系统管理员").date("2026-06-17").complete(true).build()
                ))
                .dynamicList(List.of(
                        ConsoleDashboardVo.DynamicItem.builder()
                                .username("admin").type("登录").target("系统").build(),
                        ConsoleDashboardVo.DynamicItem.builder()
                                .username("admin").type("访问").target("工作台").build()
                ))
                .newUserList(List.of(
                        ConsoleDashboardVo.NewUserItem.builder()
                                .username("用户A").province("北京").sex(1).percentage(60).build(),
                        ConsoleDashboardVo.NewUserItem.builder()
                                .username("用户B").province("上海").sex(0).percentage(40).build()
                ))
                .build();
    }
}
