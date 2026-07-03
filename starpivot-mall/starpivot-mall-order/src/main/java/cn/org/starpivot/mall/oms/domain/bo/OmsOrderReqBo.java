package cn.org.starpivot.mall.oms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


/**
 * 订单查询请求 BO。
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
public class OmsOrderReqBo extends PageReqBo {

    /** 订单号（模糊） */
    /**
     * 订单编号
     */
    private String orderSn;

    /** 会员用户名（模糊） */
    /**
     * memberUsername
     */
    private String memberUsername;

    /** 订单状态：0待付款 1待发货 2已发货 3已完成 4已关闭 5无效 */
    /**
     * 状态
     */
    private Integer status;

    /**
     * start时间
     */
    private LocalDateTime startTime;

    /**
     * end时间
     */
    private LocalDateTime endTime;
}
