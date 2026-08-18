package cn.org.starpivot.ai.domain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiKnowledgeReindexResultVo {

    /** 已提交重建的文档数 */
    private int submitted;

    /** 跳过（无内容/不可索引）文档数 */
    private int skipped;
}
