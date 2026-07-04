package cn.org.starpivot.api.approval;

import cn.org.starpivot.api.approval.dto.InternalApprovalSubmitRequest;
import cn.org.starpivot.api.approval.vo.ApprovalTimelineVo;
import cn.org.starpivot.api.fallback.ApprovalInternalClientFallbackFactory;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 审批中台内部 Feign 客户端（服务间直连，不经网关）。
 */
@FeignClient(
        name = "starpivot-approval",
        contextId = "approvalInternalClient",
        path = "/api/${starpivot.api.version:v1}",
        fallbackFactory = ApprovalInternalClientFallbackFactory.class)
public interface ApprovalInternalClient {

    @PostMapping("/internal/approval/instance/submit")
    Result<Long> submit(@RequestBody InternalApprovalSubmitRequest request);

    @PostMapping("/internal/approval/instance/{id}/withdraw")
    Result<Void> withdraw(@PathVariable("id") Long instanceId);

    @GetMapping("/internal/approval/instance/{id}/timeline")
    Result<ApprovalTimelineVo> timeline(@PathVariable("id") Long instanceId);
}
