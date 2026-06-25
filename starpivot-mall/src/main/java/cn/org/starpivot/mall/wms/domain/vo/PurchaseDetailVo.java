package cn.org.starpivot.mall.wms.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 采购明细视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PurchaseDetailVo {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * Purchase ID
     */
    private Long purchaseId;

    /**
     * SKU ID
     */
    private Long skuId;

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

    /**
     * 状态
     */
    private Integer status;
}
