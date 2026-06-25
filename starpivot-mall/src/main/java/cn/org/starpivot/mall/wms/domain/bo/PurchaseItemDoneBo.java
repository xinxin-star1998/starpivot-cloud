package cn.org.starpivot.mall.wms.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Purchaseitemdone请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PurchaseItemDoneBo {

    /**
     * Item ID
     */
    /**
     * item ID
     */
    @NotNull(message = "采购明细ID不能为空")
    /**
     * item ID
     */
    private Long itemId;

    /** 3完成 4失败 */
    /**
     * 状态
     */
    /**
     * status
     */
    @NotNull(message = "明细状态不能为空")
    /**
     * status
     */
    private Integer status;
}
