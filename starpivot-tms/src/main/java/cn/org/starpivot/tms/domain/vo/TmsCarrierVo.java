package cn.org.starpivot.tms.domain.vo;

import lombok.Data;

@Data
public class TmsCarrierVo {
    private Long id;
    private String carrierCode;
    private String carrierName;
    private String kuaidi100Com;
    private Integer sortOrder;
    private String status;
    private String remark;
}
