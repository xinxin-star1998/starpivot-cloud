package cn.org.starpivot.api.system.dto;

import lombok.Data;

/**
 * 审批人解析请求（内部 Feign）。
 */
@Data
public class AssigneeResolveRequest {

    /** 策略类型：STARTER / DEPT_LEADER / ROLE / POST / USER */
    private String assigneeType;

    /** 策略参数（角色 key、岗位 code、用户 ID 等） */
    private String assigneeValue;

    /** 发起人用户 ID */
    private Long starterId;
}
