package cn.org.starpivot.mall.sms.domain.bo;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class SpuBoundsSaveBo {

    private Long id;

    @NotNull(message = "SPU ID不能为空")
    private Long spuId;

    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private Integer work;
}
