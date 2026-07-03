package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/** SKU 改价 */

/**
 * SKU 价格请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class SkuPriceBo {

    /**
     * SKU ID
     */
    /**
     * SKU ID
     */
    @NotNull(message = "SKU ID 不能为空")
    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * price
     */
    /**
     * price
     */
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.0", inclusive = true, message = "价格不能为负数")
    /**
     * price
     */
    private BigDecimal price;
}
