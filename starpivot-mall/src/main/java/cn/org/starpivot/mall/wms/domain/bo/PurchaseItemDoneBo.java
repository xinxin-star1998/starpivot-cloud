package cn.org.starpivot.mall.wms.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PurchaseItemDoneBo {

    @NotNull(message = "采购明细ID不能为空")
    private Long itemId;

    /** 3完成 4失败 */
    @NotNull(message = "明细状态不能为空")
    private Integer status;
}
