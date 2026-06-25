package cn.org.starpivot.mall.oms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 订单设置实体。
 * <p>
 * 对应数据库表 {@code oms_order_setting}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("oms_order_setting")
public class OmsOrderSetting {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * flashOrderOvertime
     */
    private Integer flashOrderOvertime;

    /**
     * normalOrderOvertime
     */
    private Integer normalOrderOvertime;

    /**
     * confirmOvertime
     */
    private Integer confirmOvertime;

    /**
     * finishOvertime
     */
    private Integer finishOvertime;

    /**
     * commentOvertime
     */
    private Integer commentOvertime;

    /**
     * member Level
     */
    private Integer memberLevel;

}
