package cn.org.starpivot.ai.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiKnowledgeBaseVo {

    private Long kbId;

    private String kbName;

    private String description;

    private Integer topK;

    private Integer chunkSize;

    private Integer chunkOverlap;

    private String status;

    private LocalDateTime updateTime;
}
