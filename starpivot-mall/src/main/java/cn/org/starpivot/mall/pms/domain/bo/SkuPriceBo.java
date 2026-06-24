package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

/** SKU 改价 */
@Data
public class SkuPriceBo {

    @NotNull(message = "SKU ID 不能为空")
    private Long skuId;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.0", inclusive = true, message = "价格不能为负数")
    private BigDecimal price;
}
