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

    /** 文档总数 */
    private Integer docCount;

    /** 分块总数 */
    private Integer chunkCount;

    /** 已索引文档数 */
    private Integer indexedCount;

    /** 索引中/待索引文档数 */
    private Integer indexingCount;

    /** 索引失败文档数 */
    private Integer failedCount;
}
