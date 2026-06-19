package cn.org.starpivot.monitor.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.monitor.domain.vo.ServerInfoVO;
import cn.org.starpivot.monitor.service.MonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/monitor/server")
@RequiredArgsConstructor
public class ServerMonitorController {

    private final MonitorService monitorService;

    @Log(title = "服务器监控")
    @PreAuthorize("hasAuthority('monitor:server:query')")
    @GetMapping
    public Result<ServerInfoVO> getServerInfo() {
        return Result.success(monitorService.getServerInfo());
    }
}
