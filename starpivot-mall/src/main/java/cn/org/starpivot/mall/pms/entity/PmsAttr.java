package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商品属性实体。
 * <p>
 * 对应数据库表 {@code pms_attr}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("pms_attr")
public class PmsAttr {

    /**
     * 属性 ID
     */

    @TableId(value = "attr_id", type = IdType.AUTO)
    private Long attrId;

    /**
     * 属性名称
     */
    private String attrName;

    /**
     * 类型
     */
    private Integer searchType;

    /**
     * 类型
     */
    private Integer valueType;

    /**
     * icon
     */
    private String icon;

    /**
     * value Select
     */
    private String valueSelect;

    /**
     * 属性类型
     */
    private Integer attrType;

    /**
     * enable
     */
    private Long enable;

    /**
     * 分类 ID
     */
    private Long catelogId;

    /**
     * show Desc
     */
    private Integer showDesc;

}
