package cn.org.starpivot.approval.controller;

import cn.org.starpivot.approval.domain.dto.ApNotificationQueryDto;
import cn.org.starpivot.approval.domain.vo.ApNotificationVo;
import cn.org.starpivot.approval.service.ApNotificationService;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/approval/notification")
@RequiredArgsConstructor
@Tag(name = "审批通知", description = "站内通知")
public class ApNotificationController {

    private final ApNotificationService notificationService;

    @Operation(summary = "我的通知分页")
    @PostMapping("/notificationPageList")
    public Result<PageResponse<ApNotificationVo>> pageList(@RequestBody ApNotificationQueryDto query) {
        return Result.success(notificationService.pageList(query, requireUserId()));
    }

    @Operation(summary = "未读数量")
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(notificationService.unreadCount(requireUserId()));
    }

    @Operation(summary = "标记已读")
    @PostMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id, requireUserId());
        return Result.success();
    }

    @Operation(summary = "全部已读")
    @PostMapping("/read-all")
    public Result<Void> markAllRead() {
        notificationService.markAllRead(requireUserId());
        return Result.success();
    }

    private Long requireUserId() {
        Long userId = SecurityContextUtils.getUserId();
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "未登录");
        }
        return userId;
    }
}
