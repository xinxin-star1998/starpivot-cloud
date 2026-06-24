package cn.org.starpivot.mall.oms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 退款流水查询（oms_refund_info）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RefundReqBo extends PageReqBo {

    private String orderSn;
}
