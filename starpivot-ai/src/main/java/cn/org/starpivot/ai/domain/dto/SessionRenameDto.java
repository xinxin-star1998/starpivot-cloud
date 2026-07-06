package cn.org.starpivot.ai.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SessionRenameDto {

    @NotBlank(message = "会话 ID 不能为空")
    private String conversationId;

    @NotBlank(message = "标题不能为空")
    @Size(max = 64, message = "标题不能超过64字")
    private String title;
}
