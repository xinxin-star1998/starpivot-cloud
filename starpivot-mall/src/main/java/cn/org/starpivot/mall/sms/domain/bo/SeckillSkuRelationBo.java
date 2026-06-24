package cn.org.starpivot.mall.sms.domain.bo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SeckillSkuRelationBo {

    private Long id;
    private Long promotionSessionId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer seckillCount;
    private Integer seckillLimit;
    private Integer seckillSort;
}
