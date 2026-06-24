package cn.org.starpivot.mall.sms.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SeckillSkuRelationVo {

    private Long id;
    private Long promotionId;
    private Long promotionSessionId;
    private String sessionName;
    private Long skuId;
    private String skuName;
    private BigDecimal seckillPrice;
    private Integer seckillCount;
    private Integer seckillLimit;
    private Integer seckillSort;
}
