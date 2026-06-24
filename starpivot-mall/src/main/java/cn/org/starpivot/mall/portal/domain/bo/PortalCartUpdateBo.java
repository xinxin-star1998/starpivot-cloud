package cn.org.starpivot.mall.portal.domain.bo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PortalCartUpdateBo {

    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;

    /** 是否选中结算，不传则不修改 */
    private Boolean checked;
}
