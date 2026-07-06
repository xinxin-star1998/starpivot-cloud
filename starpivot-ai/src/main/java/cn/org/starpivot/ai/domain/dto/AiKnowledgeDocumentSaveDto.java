package cn.org.starpivot.ai.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiKnowledgeDocumentSaveDto {

    private Long docId;

    @NotNull(message = "知识库ID不能为空")
    private Long kbId;

    @NotBlank(message = "文档标题不能为空")
    @Size(max = 256, message = "标题不能超过256字")
    private String title;

    @NotBlank(message = "文档内容不能为空")
    private String content;

    private String status;
}
