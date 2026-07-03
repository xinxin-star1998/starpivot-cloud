package cn.org.starpivot.mall.wms.domain.bo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购明细保存请求 BO。
 * <p>
 * 用于新增或修改接口的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PurchaseDetailSaveBo {

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
     * sku Num
     */
    /**
     * sku Num
     */
    @NotNull(message = "采购数量不能为空")
    @Min(value = 1, message = "采购数量至少为1")
    /**
     * sku Num
     */
    private Integer skuNum;

    /**
     * sku Price
     */
    private BigDecimal skuPrice;

    /**
     * Ware ID
     */
    private Long wareId;
}
