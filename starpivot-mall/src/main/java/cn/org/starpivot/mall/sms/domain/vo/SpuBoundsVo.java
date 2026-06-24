package cn.org.starpivot.mall.sms.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SpuBoundsVo {

    private Long id;
    private Long spuId;
    private String spuName;
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private Integer work;
}
