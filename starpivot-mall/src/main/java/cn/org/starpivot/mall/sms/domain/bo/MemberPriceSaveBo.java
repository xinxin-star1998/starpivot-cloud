package cn.org.starpivot.mall.sms.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员价格保存请求 BO。
 * <p>
 * 用于新增或修改接口的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class MemberPriceSaveBo {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * SKU ID
     */
    /**
     * SKU ID
     */
    @NotNull(message = "SKU ID不能为空")
    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * MemberLevel ID
     */
    /**
     * memberLevel ID
     */
    @NotNull(message = "会员等级不能为空")
    /**
     * memberLevel ID
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
