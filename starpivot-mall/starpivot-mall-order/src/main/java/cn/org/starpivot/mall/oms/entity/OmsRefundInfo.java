package cn.org.starpivot.mall.oms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 退款信息实体。
 * <p>
 * 对应数据库表 {@code oms_refund_info}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("oms_refund_info")
public class OmsRefundInfo {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * OrderReturn ID
     */
    private Long orderReturnId;

    /**
     * refund
     */
    private BigDecimal refund;

    /**
     * refund Sn
     */
    private String refundSn;

    /**
     * 状态
     */
    private Integer refundStatus;

    /**
     * refund Channel
     */
    private Integer refundChannel;

    /**
     * refund Content
     */
    private String refundContent;

    /**
     * 失败告警已读：0未读 1已读
     */
    private Integer alertAck;

}
