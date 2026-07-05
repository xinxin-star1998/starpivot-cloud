package cn.org.starpivot.tms.domain.dto;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TmsCarrierQueryDto extends PageReqBo {
    private String carrierCode;
    private String carrierName;
    private String status;
}
