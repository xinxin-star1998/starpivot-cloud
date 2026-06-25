package cn.org.starpivot.mall.wms.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 仓库工单实体。
 * <p>
 * 对应数据库表 {@code wms_ware_order_task}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("wms_ware_order_task")
public class WmsWareOrderTask {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
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

}
