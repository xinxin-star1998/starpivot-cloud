package cn.org.starpivot.mall.oms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


/**
 * 退货查询请求 BO。
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
public class ReturnReqBo extends PageReqBo {

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * memberUsername
     */
    private String memberUsername;

    /** 0待处理 1退货中 2已完成 3已拒绝 */
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
