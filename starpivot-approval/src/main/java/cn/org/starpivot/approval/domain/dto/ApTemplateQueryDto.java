package cn.org.starpivot.approval.domain.dto;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApTemplateQueryDto extends PageReqBo {

    private String templateCode;
    private String templateName;
    private String bizModule;
    private String status;
}
