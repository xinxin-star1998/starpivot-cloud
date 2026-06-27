package cn.org.starpivot.api.approval;

import cn.org.starpivot.api.approval.dto.ApprovalSubmitRequest;
import cn.org.starpivot.api.approval.vo.ApprovalTimelineVo;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 审批中台 Feign 客户端。
 */
@FeignClient(
        name = "starpivot-approval",
        contextId = "approvalClient",
        path = "/api/${starpivot.api.version:v1}")
public interface ApprovalClient {

    @PostMapping("/approval/instance/submit")
    Result<Long> submit(@RequestBody ApprovalSubmitRequest request);

    @GetMapping("/approval/instance/{id}/timeline")
    Result<ApprovalTimelineVo> timeline(@PathVariable("id") Long id);
}
