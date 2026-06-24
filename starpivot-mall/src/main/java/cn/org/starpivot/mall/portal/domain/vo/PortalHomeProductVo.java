package cn.org.starpivot.mall.portal.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PortalHomeProductVo {

    private Long spuId;

    private Long skuId;

    private String spuName;

    private String coverImg;

    /** 原价 / 常规售价 */
    private BigDecimal price;

    /** 促销价（秒杀价等） */
    private BigDecimal promoPrice;
}
