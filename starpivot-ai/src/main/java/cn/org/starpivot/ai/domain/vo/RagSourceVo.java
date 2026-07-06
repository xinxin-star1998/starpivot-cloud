package cn.org.starpivot.ai.domain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RagSourceVo {

    private Long chunkId;

    private Long docId;

    private String docTitle;

    private String snippet;

    private Double score;

    private Integer pageNum;
}
