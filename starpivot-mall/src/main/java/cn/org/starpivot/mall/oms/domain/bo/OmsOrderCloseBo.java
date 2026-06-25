package cn.org.starpivot.mall.oms.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Orderclose请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class OmsOrderCloseBo {

    /**
     * Order ID
     */
    /**
     * order ID
     */
    @NotNull(message = "订单ID不能为空")
    /**
     * order ID
     */
    private Long orderId;
}
