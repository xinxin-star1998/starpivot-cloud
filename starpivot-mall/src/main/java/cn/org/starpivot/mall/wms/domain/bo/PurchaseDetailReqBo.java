package cn.org.starpivot.mall.wms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 采购明细查询请求 BO。
 * <p>
 * 用于分页查询或列表筛选的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link EqualsAndHashCode} — 继承父类字段参与 equals/hashCode</li>
 * </ul>
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseDetailReqBo extends PageReqBo {

    /**
     * 状态
     */
    private Integer status;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * Purchase ID
     */
    private Long purchaseId;

    /** 仅查询未合并的采购需求 */
    /**
     * only Demand
     */
    private Boolean onlyDemand;
}
