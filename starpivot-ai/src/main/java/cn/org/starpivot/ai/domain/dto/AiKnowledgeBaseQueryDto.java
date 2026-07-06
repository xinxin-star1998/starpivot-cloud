package cn.org.starpivot.ai.domain.dto;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AiKnowledgeBaseQueryDto extends PageReqBo {

    private String kbName;

    private String status;
}
