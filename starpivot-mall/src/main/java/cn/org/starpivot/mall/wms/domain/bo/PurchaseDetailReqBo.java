package cn.org.starpivot.mall.wms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseDetailReqBo extends PageReqBo {

    private Integer status;

    private Long skuId;

    private Long purchaseId;

    /** 仅查询未合并的采购需求 */
    private Boolean onlyDemand;
}
