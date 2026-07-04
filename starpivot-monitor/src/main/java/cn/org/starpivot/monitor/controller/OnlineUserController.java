package cn.org.starpivot.monitor.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.monitor.domain.vo.OnlineUserVO;
import cn.org.starpivot.monitor.service.MonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在线用户监控与管理 REST 接口。
 * <p>
 * {@link RestController}：返回 JSON 响应；
 * {@link RequestMapping}：统一前缀 {@code /monitor/online}；
 * {@link RequiredArgsConstructor}：注入 {@link MonitorService}。
 */
@RestController
@RequestMapping("/monitor/online")
@RequiredArgsConstructor
public class OnlineUserController {

    private final MonitorService monitorService;

    /**
     * 按条件查询当前在线用户列表。
     *
     * @param userName 用户名（可选，模糊匹配）
     * @param ipaddr   登录 IP（可选，模糊匹配）
     * @return 在线用户列表
     */
    @Log(title = "在线用户")
    @PreAuthorize("hasAuthority('monitor:online:query')")
    @GetMapping
    public Result<List<OnlineUserVO>> getOnlineUserList(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String ipaddr) {
        return Result.success(monitorService.getOnlineUserList(userName, ipaddr));
    }

    /**
     * 强制指定会话下线。
     *
     * @param sessionId 会话标识
     * @return 操作结果
     */
    @Log(title = "强退在线用户", businessType = BusinessType.FORCE)
    @PreAuthorize("hasAuthority('monitor:online:force-logout')")
    @DeleteMapping("/{sessionId}")
    public Result<?> forceLogout(@PathVariable("sessionId") String sessionId) {
        boolean success = monitorService.forceLogout(sessionId);
        return success ? Result.success("强制下线成功") : Result.error("强制下线失败");
    }
}
