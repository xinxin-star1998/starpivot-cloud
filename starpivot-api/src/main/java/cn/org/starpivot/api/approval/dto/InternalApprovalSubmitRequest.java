package cn.org.starpivot.api.approval.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务间提交审批请求（含发起人 ID，不经用户 JWT 鉴权）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InternalApprovalSubmitRequest extends ApprovalSubmitRequest {

    @NotNull(message = "starterId 不能为空")
    private Long starterId;
}
