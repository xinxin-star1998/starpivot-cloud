package cn.org.starpivot.mall.sms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员价格视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class MemberPriceVo {

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
     * MemberLevel ID
     */
    private Long memberLevelId;
    /**
     * memberLevel名称
     */
    private String memberLevelName;
    /**
     * member Price
     */
    private BigDecimal memberPrice;
    /**
     * add Other
     */
    private Integer addOther;
}
