package cn.org.starpivot.approval.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ApTemplateSaveDto {

    private Long templateId;

    @NotBlank(message = "templateCode 不能为空")
    private String templateCode;

    @NotBlank(message = "templateName 不能为空")
    private String templateName;

    @NotBlank(message = "bizModule 不能为空")
    private String bizModule;

    private String remark;

    @Valid
    @NotNull(message = "steps 不能为空")
    private List<ApTemplateStepDto> steps;

    /** 步骤间条件路由（可选） */
    private List<ApTemplateRouteDto> routes;

    @Data
    public static class ApTemplateRouteDto {
        private String fromStepCode;
        private String toStepCode;
        private Integer priority;
        private String conditionExpr;
    }

    @Data
    public static class ApTemplateStepDto {
        private Long stepId;
        @NotBlank(message = "stepCode 不能为空")
        private String stepCode;
        @NotBlank(message = "stepName 不能为空")
        private String stepName;
        @NotNull(message = "stepOrder 不能为空")
        private Integer stepOrder;
        @NotBlank(message = "assigneeType 不能为空")
        private String assigneeType;
        private String assigneeValue;
        private String approveMode;
        private String skipExpression;
        private Integer timeoutHours;
        private String timeoutAction;
    }
}
