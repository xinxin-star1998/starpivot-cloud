package cn.org.starpivot.approval.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApTaskActionDto {

    @NotNull(message = "taskId 不能为空")
    private Long taskId;
    private String comment;
}
