package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车项视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PortalCartItemVo {

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * sku Title
     */
    private String skuTitle;

    /**
     * 图片
     */
    private String skuDefaultImg;

    /**
     * price
     */
    private BigDecimal price;

    /**
     * quantity
     */
    private Integer quantity;

    /**
     * checked
     */
    private Boolean checked;

    /** 销售属性文案，如 颜色:红;尺寸:L */
    /**
     * sku Attr
     */
    private String skuAttr;

    /** 可售库存 */
    /**
     * stock
     */
    private Integer stock;

    /**
     * Valid
     */
    private Boolean valid;
}
