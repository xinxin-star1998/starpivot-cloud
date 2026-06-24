package cn.org.starpivot.mall.portal.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PortalCartItemVo {

    private Long skuId;

    private Long spuId;

    private String skuTitle;

    private String skuDefaultImg;

    private BigDecimal price;

    private Integer quantity;

    private Boolean checked;

    /** 销售属性文案，如 颜色:红;尺寸:L */
    private String skuAttr;

    /** 可售库存 */
    private Integer stock;

    private Boolean valid;
}
