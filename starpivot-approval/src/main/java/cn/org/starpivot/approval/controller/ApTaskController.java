package cn.org.starpivot.approval.controller;

import cn.org.starpivot.approval.domain.dto.ApTaskActionDto;
import cn.org.starpivot.approval.domain.dto.ApTaskQueryDto;
import cn.org.starpivot.approval.domain.vo.ApTaskVo;
import cn.org.starpivot.approval.service.ApTaskService;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/approval/task")
@RequiredArgsConstructor
@Tag(name = "审批任务", description = "待办、已办、通过、驳回")
public class ApTaskController {

    private final ApTaskService taskService;

    @PreAuthorize("hasAuthority('approval:task:query')")
    @PostMapping("/todoTaskPageList")
    public Result<PageResponse<ApTaskVo>> todoList(@RequestBody ApTaskQueryDto query) {
        return Result.success(taskService.todoList(query, requireUserId()));
    }

    @PreAuthorize("hasAuthority('approval:task:query')")
    @PostMapping("/doneTaskPageList")
    public Result<PageResponse<ApTaskVo>> doneList(@RequestBody ApTaskQueryDto query) {
        return Result.success(taskService.doneList(query, requireUserId()));
    }

    @PreAuthorize("hasAuthority('approval:task:action')")
    @PostMapping("/approve")
    public Result<Void> approve(@Valid @RequestBody ApTaskActionDto dto) {
        taskService.approve(dto, requireUserId());
        return Result.success();
    }

    @PreAuthorize("hasAuthority('approval:task:action')")
    @PostMapping("/reject")
    public Result<Void> reject(@Valid @RequestBody ApTaskActionDto dto) {
        taskService.reject(dto, requireUserId());
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
