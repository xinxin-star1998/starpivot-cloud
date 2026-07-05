package cn.org.starpivot.tms.domain.dto;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TmsFreightRuleQueryDto extends PageReqBo {
    private String ruleName;
    private String ruleType;
    private String status;
}
