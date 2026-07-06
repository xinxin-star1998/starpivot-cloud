package cn.org.starpivot.ai.domain.dto;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AiConfigQueryDto extends PageReqBo {

    private String configName;

    private String botName;

    /** 状态：0正常 1停用 */
    private String status;
}
