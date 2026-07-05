package cn.org.starpivot.api.tms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商城订单发货同步请求（TMS → mall-order 内部接口）。
 */
@Data
public class InternalOrderDeliverRequest {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotBlank(message = "物流公司不能为空")
    private String deliveryCompany;

    @NotBlank(message = "物流单号不能为空")
    private String deliverySn;
}
