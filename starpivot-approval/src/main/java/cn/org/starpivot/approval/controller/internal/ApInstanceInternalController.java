package cn.org.starpivot.approval.controller.internal;

import cn.org.starpivot.api.approval.dto.InternalApprovalSubmitRequest;
import cn.org.starpivot.approval.service.ApInstanceService;
import cn.org.starpivot.common.domain.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 审批实例内部接口，供 mall 等业务服务通过 Feign 调用。
 */
@RestController
@RequestMapping("/internal/approval/instance")
@RequiredArgsConstructor
public class ApInstanceInternalController {

    private final ApInstanceService instanceService;

    @PostMapping("/submit")
    public Result<Long> submit(@Valid @RequestBody InternalApprovalSubmitRequest request) {
        return Result.success(instanceService.submit(request, request.getStarterId()));
    }

    @PostMapping("/{id}/withdraw")
    public Result<Void> withdraw(@PathVariable Long id) {
        instanceService.systemWithdraw(id);
        return Result.success();
    }
}
