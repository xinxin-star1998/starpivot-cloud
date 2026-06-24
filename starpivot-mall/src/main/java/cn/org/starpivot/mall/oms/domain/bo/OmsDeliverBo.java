package cn.org.starpivot.mall.oms.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单发货
 */
@Data
public class OmsDeliverBo {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotBlank(message = "物流公司不能为空")
    private String deliveryCompany;

    @NotBlank(message = "物流单号不能为空")
    private String deliverySn;
}
