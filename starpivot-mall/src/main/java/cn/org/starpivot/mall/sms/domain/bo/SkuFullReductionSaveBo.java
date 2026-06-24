package cn.org.starpivot.mall.sms.domain.bo;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class SkuFullReductionSaveBo {

    private Long id;

    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer addOther;
}
