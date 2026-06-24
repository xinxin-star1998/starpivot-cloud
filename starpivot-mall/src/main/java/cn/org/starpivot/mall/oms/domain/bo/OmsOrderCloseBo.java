package cn.org.starpivot.mall.oms.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 关闭订单
 */
@Data
public class OmsOrderCloseBo {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;
}
