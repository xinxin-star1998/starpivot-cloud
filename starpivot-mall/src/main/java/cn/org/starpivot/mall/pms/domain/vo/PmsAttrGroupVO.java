package cn.org.starpivot.mall.pms.domain.vo;

import lombok.Data;

/**
 * 属性分组VO
 * 
 * @author admin
 * @since 2026-05-18
 */
@Data
public class PmsAttrGroupVO {

    /**
     * 分组id
     */
    private Long attrGroupId;

    /**
     * 组名
     */
    private String attrGroupName;

    /**
     * 排序
     */
    private Long sort;

    /**
     * 描述
     */
    private String descript;

    /**
     * 组图标
     */
    private String icon;

    /**
     * 所属分类id
     */
    private Long catelogId;

}
