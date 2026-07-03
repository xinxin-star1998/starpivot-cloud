package cn.org.starpivot.api.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审批人解析请求（内部 Feign）。
 */
@Data
public class AssigneeResolveRequest {

    /** 策略类型：STARTER / DEPT_LEADER / ROLE / POST / USER */
    @NotBlank(message = "审批人类型不能为空")
    private String assigneeType;

    /** 策略参数（角色 key、岗位 code、用户 ID 等） */
    private String assigneeValue;

    /** 发起人用户 ID */
    @NotNull(message = "发起人用户ID不能为空")
    private Long starterId;
}
