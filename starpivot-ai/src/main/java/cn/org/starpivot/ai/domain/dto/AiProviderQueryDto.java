package cn.org.starpivot.ai.domain.dto;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AiProviderQueryDto extends PageReqBo {

    private String providerName;

    private String providerCode;

    private String status;
}
