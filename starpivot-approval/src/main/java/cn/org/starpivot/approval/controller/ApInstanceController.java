package cn.org.starpivot.approval.controller;

import cn.org.starpivot.api.approval.dto.ApprovalSubmitRequest;
import cn.org.starpivot.api.approval.vo.ApprovalTimelineVo;
import cn.org.starpivot.approval.domain.dto.ApInstanceQueryDto;
import cn.org.starpivot.approval.domain.vo.ApInstanceVo;
import cn.org.starpivot.approval.service.ApInstanceService;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/approval/instance")
@RequiredArgsConstructor
@Tag(name = "审批实例", description = "提交、撤回、我的申请、时间轴")
public class ApInstanceController {

    private final ApInstanceService instanceService;

    @PreAuthorize("hasAuthority('approval:instance:submit')")
    @PostMapping("/submit")
    public Result<Long> submit(@Valid @RequestBody ApprovalSubmitRequest request) {
        return Result.success(instanceService.submit(request, requireUserId()));
    }

    @PreAuthorize("hasAuthority('approval:instance:withdraw')")
    @PostMapping("/{id}/withdraw")
    public Result<Void> withdraw(@PathVariable Long id) {
        instanceService.withdraw(id, requireUserId());
        return Result.success();
    }

    @PreAuthorize("hasAuthority('approval:instance:query')")
    @PostMapping("/mineInstancePageList")
    public Result<PageResponse<ApInstanceVo>> mineList(@RequestBody ApInstanceQueryDto query) {
        return Result.success(instanceService.mineList(query, requireUserId()));
    }

    @PreAuthorize("hasAuthority('approval:instance:query')")
    @GetMapping("/{id}/timeline")
    public Result<ApprovalTimelineVo> timeline(@PathVariable Long id) {
        return Result.success(instanceService.timeline(id, requireUserId()));
    }

    private Long requireUserId() {
        Long userId = SecurityContextUtils.getUserId();
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "未登录");
        }
        return userId;
    }
}
