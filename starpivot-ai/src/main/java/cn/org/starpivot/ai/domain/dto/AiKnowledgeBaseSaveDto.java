package cn.org.starpivot.ai.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiKnowledgeBaseSaveDto {

    private Long kbId;

    @NotBlank(message = "知识库名称不能为空")
    @Size(max = 128, message = "知识库名称不能超过128字")
    private String kbName;

    @Size(max = 500, message = "描述不能超过500字")
    private String description;

    @Min(value = 1, message = "检索条数至少为1")
    @Max(value = 20, message = "检索条数不能超过20")
    private Integer topK;

    @Min(value = 200, message = "分块大小至少200")
    @Max(value = 4000, message = "分块大小不能超过4000")
    private Integer chunkSize;

    @Min(value = 0, message = "重叠不能小于0")
    @Max(value = 500, message = "重叠不能超过500")
    private Integer chunkOverlap;

    private String status;
}
