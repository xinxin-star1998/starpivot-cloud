package cn.org.starpivot.api.approval.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 提交审批请求。
 */
@Data
public class ApprovalSubmitRequest {

    @NotBlank(message = "bizModule 不能为空")
    private String bizModule;

    @NotBlank(message = "bizType 不能为空")
    private String bizType;

    @NotBlank(message = "bizKey 不能为空")
    private String bizKey;

    @NotBlank(message = "title 不能为空")
    private String title;

    /** 显式指定模板编码；为空时走 ap_template_bind 自动匹配 */
    private String templateCode;

    /** 审批上下文，供 SpEL 条件与展示 */
    private Map<String, Object> context;
}
