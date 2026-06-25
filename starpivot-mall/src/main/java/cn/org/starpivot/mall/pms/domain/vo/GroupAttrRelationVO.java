package cn.org.starpivot.mall.pms.domain.vo;

import lombok.Data;

/**
 * 分组属性关联视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class GroupAttrRelationVO {

    /**
     * 属性 ID
     */
    private Long attrId;

    /**
     * 属性名称
     */
    private String attrName;

    /** 属性图标 */
    /**
     * icon
     */
    private String icon;

    /** 可选值（分号分隔，多值时有值） */
    /**
     * value Select
     */
    private String valueSelect;

    /** 是否已关联到当前分组 */
    /**
     * linked
     */
    private Boolean linked;

    /** 组内排序（已关联时有值） */
    /**
     * attr Sort
     */
    private Integer attrSort;
}
