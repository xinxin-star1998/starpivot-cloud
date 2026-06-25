package cn.org.starpivot.mall.wms.domain.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 仓库工单视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class WareOrderTaskVo {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * Order ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * consignee
     */
    private String consignee;

    /**
     * consignee Tel
     */
    private String consigneeTel;

    /**
     * delivery Address
     */
    private String deliveryAddress;

    /**
     * order Comment
     */
    private String orderComment;

    /**
     * payment Way
     */
    private Integer paymentWay;

    /**
     * 状态
     */
    private Integer taskStatus;

    /**
     * order Body
     */
    private String orderBody;

    /**
     * tracking No
     */
    private String trackingNo;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * Ware ID
     */
    private Long wareId;

    /**
     * task Comment
     */
    private String taskComment;

    /**
     * details
     */
    private List<WareOrderTaskDetailVo> details;
}
