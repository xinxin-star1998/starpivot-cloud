package cn.org.starpivot.monitor.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.monitor.domain.vo.OnlineUserVO;
import cn.org.starpivot.monitor.service.MonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/monitor/online")
@RequiredArgsConstructor
public class OnlineUserController {

    private final MonitorService monitorService;

    @Log(title = "在线用户")
    @PreAuthorize("hasAuthority('monitor:online:query')")
    @GetMapping
    public Result<List<OnlineUserVO>> getOnlineUserList(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String ipaddr) {
        return Result.success(monitorService.getOnlineUserList(userName, ipaddr));
    }

    @Log(title = "强退在线用户", businessType = BusinessType.FORCE)
    @PreAuthorize("hasAuthority('monitor:online:force-logout')")
    @DeleteMapping("/{sessionId}")
    public Result<?> forceLogout(@PathVariable("sessionId") String sessionId) {
        boolean success = monitorService.forceLogout(sessionId);
        return success ? Result.success("强制下线成功") : Result.error("强制下线失败");
    }
}
