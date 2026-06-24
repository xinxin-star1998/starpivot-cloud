package cn.org.starpivot.mall.sms.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SkuLadderVo {

    private Long id;
    private Long skuId;
    private String skuName;
    private Integer fullCount;
    private BigDecimal discount;
    private BigDecimal price;
    private Integer addOther;
}
