package cn.org.starpivot.mall.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 仓库 SKU 库存实体。
 * <p>
 * 对应数据库表 {@code wms_ware_sku}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("wms_ware_sku")
public class WmsWareSku {

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
     * Ware ID
     */
    private Long wareId;

    /**
     * stock
     */
    private Integer stock;

    /**
     * sku名称
     */
    private String skuName;

    /**
     * stock Locked
     */
    private Integer stockLocked;

}
