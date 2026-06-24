package cn.org.starpivot.mall.sms.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SkuFullReductionVo {

    private Long id;
    private Long skuId;
    private String skuName;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer addOther;
}
