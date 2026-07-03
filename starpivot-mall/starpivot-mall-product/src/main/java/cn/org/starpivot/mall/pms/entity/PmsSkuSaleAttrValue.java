package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * SKU 销售属性值实体。
 * <p>
 * 对应数据库表 {@code pms_sku_sale_attr_value}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("pms_sku_sale_attr_value")
public class PmsSkuSaleAttrValue {

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
     * 属性 ID
     */
    private Long attrId;

    /**
     * 属性名称
     */
    private String attrName;

    /**
     * attr Value
     */
    private String attrValue;

    /**
     * attr Sort
     */
    private Integer attrSort;

}
