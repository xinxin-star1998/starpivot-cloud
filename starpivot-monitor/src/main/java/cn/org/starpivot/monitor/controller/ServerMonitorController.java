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

/**
 * 服务器运行状态监控 REST 接口。
 * <p>
 * {@link RestController}：返回 JSON 响应；
 * {@link RequestMapping}：统一前缀 {@code /monitor/server}；
 * {@link RequiredArgsConstructor}：注入 {@link MonitorService}。
 */
@RestController
@RequestMapping("/monitor/server")
@RequiredArgsConstructor
public class ServerMonitorController {

    private final MonitorService monitorService;

    /**
     * 查询当前服务器 CPU、内存、磁盘等信息。
     *
     * @return 包装后的服务器监控数据
     */
    @Log(title = "服务器监控")
    @PreAuthorize("hasAuthority('monitor:server:query')")
    @GetMapping
    public Result<ServerInfoVO> getServerInfo() {
        return Result.success(monitorService.getServerInfo());
    }
}
