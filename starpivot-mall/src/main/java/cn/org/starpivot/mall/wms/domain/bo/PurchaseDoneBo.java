package cn.org.starpivot.mall.wms.domain.bo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class PurchaseDoneBo {

    @NotNull(message = "采购单ID不能为空")
    private Long id;

    @NotEmpty(message = "采购明细不能为空")
    private List<PurchaseItemDoneBo> items;
}
