package cn.org.starpivot.tms.domain.dto;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TmsShipmentQueryDto extends PageReqBo {
    private String orderSn;
    private String trackingNo;
    private String status;
    private String carrierName;
}
