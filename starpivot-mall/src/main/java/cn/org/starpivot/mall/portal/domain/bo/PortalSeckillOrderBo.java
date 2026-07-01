package cn.org.starpivot.mall.portal.domain.bo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 秒杀下单请求 BO。
 */
@Data
public class PortalSeckillOrderBo {

    @NotNull(message = "秒杀场次不能为空")
    private Long sessionId;

    @NotNull(message = "SKU 不能为空")
    private Long skuId;

    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少为 1")
    private Integer quantity;

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    private String note;

    /** 支付方式，默认 1（支付宝） */
    private Integer payType;

    @NotBlank(message = "订单提交令牌不能为空")
    private String orderToken;
}
