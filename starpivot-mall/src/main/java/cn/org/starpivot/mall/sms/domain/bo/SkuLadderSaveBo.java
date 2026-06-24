package cn.org.starpivot.mall.sms.domain.bo;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class SkuLadderSaveBo {

    private Long id;

    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    private Integer fullCount;
    private BigDecimal discount;
    private BigDecimal price;
    private Integer addOther;
}
