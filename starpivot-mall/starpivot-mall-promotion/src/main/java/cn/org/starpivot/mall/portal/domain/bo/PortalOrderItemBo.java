package cn.org.starpivot.mall.portal.domain.bo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单明细请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PortalOrderItemBo {

    /**
     * SKU ID
     */
    /**
     * SKU ID
     */
    @NotNull(message = "SKU ID不能为空")
    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * quantity
     */
    /**
     * quantity
     */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    /**
     * quantity
     */
    private Integer quantity;
}
