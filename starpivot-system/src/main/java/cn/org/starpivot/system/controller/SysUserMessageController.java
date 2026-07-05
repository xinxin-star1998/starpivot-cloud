package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.system.domain.bo.SysUserMessageVO;
import cn.org.starpivot.system.domain.dto.MessageBroadcastRequest;
import cn.org.starpivot.system.domain.dto.SysUserMessageQueryDTO;
import cn.org.starpivot.system.service.ISysUserMessageService;
import cn.org.starpivot.system.service.MessageSseRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
@Tag(name = "站内消息", description = "全平台统一消息中心")
public class SysUserMessageController {

    private final ISysUserMessageService messageService;
    private final MessageSseRegistry messageSseRegistry;

    @Operation(summary = "我的消息分页")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/messagePageList")
    public Result<PageResponse<SysUserMessageVO>> messagePageList(@Valid @RequestBody SysUserMessageQueryDTO query) {
        return Result.success(messageService.pageMyMessages(query, requireUserId()));
    }

    @Operation(summary = "未读数量")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(messageService.unreadCount(requireUserId()));
    }

    @Operation(summary = "标记已读")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        messageService.markRead(id, requireUserId());
        return Result.success();
    }

    @Operation(summary = "全部已读")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/read-all")
    public Result<Void> markAllRead() {
        messageService.markAllRead(requireUserId());
        return Result.success();
    }

    @Operation(summary = "管理员群发")
    @PreAuthorize("@ss.hasPermission('system:message:send')")
    @PostMapping("/broadcast")
    public Result<Integer> broadcast(@Valid @RequestBody MessageBroadcastRequest request) {
        int count = messageService.broadcast(request);
        return Result.success(count);
    }

    @Operation(summary = "SSE 实时订阅")
    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/sse/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        return messageSseRegistry.connect(requireUserId());
    }

    private Long requireUserId() {
        Long userId = SecurityContextUtils.getUserId();
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "未登录");
        }
        return userId;
    }
}
