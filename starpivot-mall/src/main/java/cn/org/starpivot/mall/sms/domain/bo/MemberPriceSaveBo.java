package cn.org.starpivot.mall.sms.domain.bo;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class MemberPriceSaveBo {

    private Long id;

    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    @NotNull(message = "会员等级不能为空")
    private Long memberLevelId;

    private String memberLevelName;
    private BigDecimal memberPrice;
    private Integer addOther;
}
