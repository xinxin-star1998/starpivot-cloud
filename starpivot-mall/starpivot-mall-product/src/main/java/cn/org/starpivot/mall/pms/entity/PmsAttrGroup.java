package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 属性分组实体。
 * <p>
 * 对应数据库表 {@code pms_attr_group}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("pms_attr_group")
public class PmsAttrGroup {

    /**
     * AttrGroup ID
     */
    /**
     * 属性分组 ID
     */
    @TableId(value = "attr_group_id", type = IdType.AUTO)
    private Long attrGroupId;

    /**
     * attrGroup名称
     */
    private String attrGroupName;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * descript
     */
    private String descript;

    /**
     * icon
     */
    private String icon;

    /**
     * 分类 ID
     */
    private Long catelogId;

}
