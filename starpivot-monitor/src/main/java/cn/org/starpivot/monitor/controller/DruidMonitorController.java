package cn.org.starpivot.monitor.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.monitor.domain.vo.DruidMonitorVO;
import cn.org.starpivot.monitor.service.MonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Druid 数据源连接池监控 REST 接口。
 * <p>
 * {@link RestController}：返回 JSON 响应；
 * {@link RequestMapping}：统一前缀 {@code /monitor/druid}；
 * {@link RequiredArgsConstructor}：注入 {@link MonitorService}。
 */
@RestController
@RequestMapping("/monitor/druid")
@RequiredArgsConstructor
public class DruidMonitorController {

    private final MonitorService monitorService;

    /**
     * 查询 Druid 连接池统计信息，可选附带慢 SQL 列表。
     *
     * @param includeSlowSqlList 是否包含慢 SQL 明细，默认 {@code false}
     * @param slowSqlThreshold   慢 SQL 阈值（毫秒），仅当包含慢 SQL 时生效
     * @return 包装后的 Druid 监控数据
     */
    @Log(title = "Druid监控")
    @PreAuthorize("hasAuthority('monitor:druid:query')")
    @GetMapping
    public Result<DruidMonitorVO> getDruidMonitorInfo(
            @RequestParam(required = false, defaultValue = "false") Boolean includeSlowSqlList,
            @RequestParam(required = false) Long slowSqlThreshold) {
        if (Boolean.TRUE.equals(includeSlowSqlList)) {
            return Result.success(monitorService.getDruidMonitorInfoWithSlowSql(slowSqlThreshold));
        }
        return Result.success(monitorService.getDruidMonitorInfo());
    }
}
