package cn.org.starpivot.approval.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApTemplateBindSaveDto {

    private Long bindId;

    @NotBlank(message = "bizModule 不能为空")
    private String bizModule;

    @NotBlank(message = "bizType 不能为空")
    private String bizType;

    private String matchExpr;

    @NotBlank(message = "templateCode 不能为空")
    private String templateCode;

    private Integer priority;
    private String status;
}
