package cn.org.starpivot.mall.wms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 仓库工单查询请求 BO。
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
public class WareOrderTaskReqBo extends PageReqBo {

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 状态
     */
    private Integer taskStatus;

    /**
     * Ware ID
     */
    private Long wareId;
}
