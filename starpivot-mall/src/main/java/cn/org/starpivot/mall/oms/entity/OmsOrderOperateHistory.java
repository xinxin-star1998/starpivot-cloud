package cn.org.starpivot.mall.oms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单操作历史实体。
 * <p>
 * 对应数据库表 {@code oms_order_operate_history}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("oms_order_operate_history")
public class OmsOrderOperateHistory {

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
     * operate Man
     */
    private String operateMan;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 状态
     */
    private Integer orderStatus;

    /**
     * note
     */
    private String note;

}
