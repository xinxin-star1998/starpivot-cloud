package cn.org.starpivot.mall.oms.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发货请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class OmsDeliverBo {

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

    /**
     * delivery Company
     */
    /**
     * delivery Company
     */
    @NotBlank(message = "物流公司不能为空")
    /**
     * delivery Company
     */
    private String deliveryCompany;

    /**
     * delivery Sn
     */
    /**
     * delivery Sn
     */
    @NotBlank(message = "物流单号不能为空")
    /**
     * delivery Sn
     */
    private String deliverySn;
}
