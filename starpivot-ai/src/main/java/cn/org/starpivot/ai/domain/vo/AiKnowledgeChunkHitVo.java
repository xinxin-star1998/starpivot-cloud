package cn.org.starpivot.ai.domain.vo;

import lombok.Data;

@Data
public class AiKnowledgeChunkHitVo {

    private Long chunkId;

    private Long docId;

    private Long kbId;

    private String docTitle;

    private String content;

    private Double score;

    private Integer pageNum;

    private String embeddingJson;
}
