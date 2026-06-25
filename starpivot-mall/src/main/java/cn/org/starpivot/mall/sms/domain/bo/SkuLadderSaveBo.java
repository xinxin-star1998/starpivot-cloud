package cn.org.starpivot.mall.sms.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * SKU 阶梯价保存请求 BO。
 * <p>
 * 用于新增或修改接口的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class SkuLadderSaveBo {

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
