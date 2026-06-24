package cn.org.starpivot.mall.pms.domain.vo;

import lombok.Data;

/**
 * 分组下可关联的基本属性（含是否已关联、组内排序）。
 */
@Data
public class GroupAttrRelationVO {

    private Long attrId;

    private String attrName;

    /** 属性图标 */
    private String icon;

    /** 可选值（分号分隔，多值时有值） */
    private String valueSelect;

    /** 是否已关联到当前分组 */
    private Boolean linked;

    /** 组内排序（已关联时有值） */
    private Integer attrSort;
}
