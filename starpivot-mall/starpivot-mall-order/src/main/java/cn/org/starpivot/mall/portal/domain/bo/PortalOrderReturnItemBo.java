package cn.org.starpivot.mall.portal.domain.bo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PortalOrderReturnItemBo {

    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    @NotNull(message = "退货数量不能为空")
    @Min(value = 1, message = "退货数量至少为 1")
    private Integer quantity;
}
