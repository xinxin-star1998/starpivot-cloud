package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 属性与分组关联实体。
 * <p>
 * 对应数据库表 {@code pms_attr_attrgroup_relation}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("pms_attr_attrgroup_relation")
public class PmsAttrAttrgroupRelation {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 属性 ID
     */
    private Long attrId;

    /**
     * AttrGroup ID
     */
    private Long attrGroupId;

    /**
     * attr Sort
     */
    private Integer attrSort;

}
