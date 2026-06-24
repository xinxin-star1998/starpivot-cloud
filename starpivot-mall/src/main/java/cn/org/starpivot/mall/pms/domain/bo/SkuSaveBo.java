package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/** SKU 修改（pms_sku_info 可编辑字段） */
@Data
public class SkuSaveBo {

    @NotNull(message = "SKU ID 不能为空")
    private Long skuId;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.0", inclusive = true, message = "价格不能为负数")
    private BigDecimal price;

    private String skuTitle;

    private String skuSubtitle;

    private String skuDefaultImg;
}
