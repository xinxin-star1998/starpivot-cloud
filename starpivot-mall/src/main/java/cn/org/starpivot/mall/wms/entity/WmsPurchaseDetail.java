package cn.org.starpivot.mall.wms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 采购明细实体。
 * <p>
 * 对应数据库表 {@code wms_purchase_detail}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("wms_purchase_detail")
public class WmsPurchaseDetail {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Purchase ID
     */
    private Long purchaseId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * sku Num
     */
    private Integer skuNum;

    /**
     * sku Price
     */
    private BigDecimal skuPrice;

    /**
     * Ware ID
     */
    private Long wareId;

    /**
     * 状态
     */
    private Integer status;

}
