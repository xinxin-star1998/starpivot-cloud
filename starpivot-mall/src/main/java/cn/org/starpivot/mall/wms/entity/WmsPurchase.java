package cn.org.starpivot.mall.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购单实体。
 * <p>
 * 对应数据库表 {@code wms_purchase}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("wms_purchase")
public class WmsPurchase {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Assignee ID
     */
    private Long assigneeId;

    /**
     * assignee名称
     */
    private String assigneeName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * priority
     */
    private Integer priority;

    /**
     * 状态
     */
    private Integer status;

    /**
     * Ware ID
     */
    private Long wareId;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 审批实例 ID
     */
    private Long approvalInstanceId;

    /**
     * 审批状态
     */
    private String auditStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
