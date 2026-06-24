package cn.org.starpivot.mall.portal.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PortalOrderQueryBo extends PageReqBo {

    /** 订单状态，不传查全部 */
    private Integer status;
}
