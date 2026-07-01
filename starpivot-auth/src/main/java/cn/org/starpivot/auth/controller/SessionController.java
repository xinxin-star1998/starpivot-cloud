package cn.org.starpivot.auth.controller;

import cn.org.starpivot.auth.domain.DeviceSessionVo;
import cn.org.starpivot.auth.service.SessionService;
import cn.org.starpivot.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "会话管理")
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @Operation(summary = "获取用户活跃会话列表")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/{userId}")
    public Result<List<DeviceSessionVo>> listUserSessions(@PathVariable Long userId) {
        return Result.success(sessionService.listUserSessions(userId));
    }

    @Operation(summary = "强制下线指定会话")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{userId}/{deviceSessionId}")
    public Result<Void> forceLogoutSession(@PathVariable Long userId,
                                           @PathVariable String deviceSessionId) {
        sessionService.forceLogoutSession(userId, deviceSessionId);
        return Result.success();
    }

    @Operation(summary = "强制下线用户所有会话")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/all/{userId}")
    public Result<Void> forceLogoutAllSessions(@PathVariable Long userId) {
        sessionService.forceLogoutAllSessions(userId);
        return Result.success();
    }
}
