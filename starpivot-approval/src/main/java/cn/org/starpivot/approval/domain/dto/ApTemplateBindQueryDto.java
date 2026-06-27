package cn.org.starpivot.approval.domain.dto;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApTemplateBindQueryDto extends PageReqBo {

    private String bizModule;
    private String bizType;
}
