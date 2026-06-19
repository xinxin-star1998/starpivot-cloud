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

@RestController
@RequestMapping("/monitor/druid")
@RequiredArgsConstructor
public class DruidMonitorController {

    private final MonitorService monitorService;

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
