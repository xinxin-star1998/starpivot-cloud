package cn.org.starpivot.mall.portal.domain.bo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * C 端开团请求 BO。
 */
@Data
public class PortalGroupBuyStartBo {

    @NotNull(message = "拼团活动不能为空")
    private Long activityId;

    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少为1")
    private Integer quantity = 1;

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    @NotBlank(message = "订单提交令牌不能为空")
    private String orderToken;

    private Integer payType;

    private String note;
}
