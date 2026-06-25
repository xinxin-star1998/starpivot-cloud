package cn.org.starpivot.mall.wms.domain.bo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/**
 * Purchasedone请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PurchaseDoneBo {

    /**
     * 主键 ID
     */
    /**
     * 主键 ID
     */
    @NotNull(message = "采购单ID不能为空")
    /**
     * 主键 ID
     */
    private Long id;

    /**
     * items
     */
    /**
     * items
     */
    @NotEmpty(message = "采购明细不能为空")
    /**
     * items
     */
    private List<PurchaseItemDoneBo> items;
}
