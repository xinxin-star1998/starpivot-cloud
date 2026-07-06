package cn.org.starpivot.ai.domain.dto;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AiChatSessionQueryDto extends PageReqBo {

    private Long userId;

    private String conversationId;

    private String title;
}
