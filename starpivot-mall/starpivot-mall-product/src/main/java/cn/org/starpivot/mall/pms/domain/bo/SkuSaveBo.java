package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/** SKU 修改（pms_sku_info 可编辑字段） */

/**
 * SKU保存请求 BO。
 * <p>
 * 用于新增或修改接口的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class SkuSaveBo {

    /**
     * SKU ID
     */
    /**
     * SKU ID
     */
    @NotNull(message = "SKU ID 不能为空")
    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * price
     */
    /**
     * price
     */
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.0", inclusive = true, message = "价格不能为负数")
    /**
     * price
     */
    private BigDecimal price;

    /**
     * sku Title
     */
    private String skuTitle;

    /**
     * sku Subtitle
     */
    private String skuSubtitle;

    /**
     * 图片
     */
    private String skuDefaultImg;
}
