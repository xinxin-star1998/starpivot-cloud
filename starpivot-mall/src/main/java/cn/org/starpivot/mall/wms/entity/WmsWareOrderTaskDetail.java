package cn.org.starpivot.mall.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 仓库工单明细实体。
 * <p>
 * 对应数据库表 {@code wms_ware_order_task_detail}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("wms_ware_order_task_detail")
public class WmsWareOrderTaskDetail {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * sku名称
     */
    private String skuName;

    /**
     * sku Num
     */
    private Integer skuNum;

    /**
     * Task ID
     */
    private Long taskId;

    /**
     * Ware ID
     */
    private Long wareId;

    /**
     * 状态
     */
    private Integer lockStatus;

}
