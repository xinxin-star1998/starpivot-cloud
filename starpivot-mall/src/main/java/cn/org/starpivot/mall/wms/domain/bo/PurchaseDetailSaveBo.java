package cn.org.starpivot.mall.wms.domain.bo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PurchaseDetailSaveBo {

    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    @NotNull(message = "采购数量不能为空")
    @Min(value = 1, message = "采购数量至少为1")
    private Integer skuNum;

    private BigDecimal skuPrice;

    private Long wareId;
}
