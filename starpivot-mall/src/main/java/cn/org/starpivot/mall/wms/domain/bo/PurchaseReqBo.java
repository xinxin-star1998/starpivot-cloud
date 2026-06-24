package cn.org.starpivot.mall.wms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseReqBo extends PageReqBo {

    private Integer status;

    private String assigneeName;

    private Long wareId;
}
