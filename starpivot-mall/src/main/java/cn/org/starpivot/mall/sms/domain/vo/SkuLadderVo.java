package cn.org.starpivot.mall.sms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * SKU 阶梯价视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class SkuLadderVo {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * SKU ID
     */
    private Long skuId;
    /**
     * sku名称
     */
    private String skuName;
    /**
     * full数量
     */
    private Integer fullCount;
    /**
     * discount
     */
    private BigDecimal discount;
    /**
     * price
     */
    private BigDecimal price;
    /**
     * add Other
     */
    private Integer addOther;
}
