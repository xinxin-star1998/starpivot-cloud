package cn.org.starpivot.mall.oms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 支付流水查询（oms_payment_info）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentReqBo extends PageReqBo {

    private String orderSn;
}
