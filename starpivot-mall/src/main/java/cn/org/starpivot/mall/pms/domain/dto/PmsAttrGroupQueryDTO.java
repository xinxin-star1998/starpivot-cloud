package cn.org.starpivot.mall.pms.domain.dto;

import lombok.Data;

/**
 * 属性分组查询DTO
 * 
 * @author admin
 * @since 2026-05-18
 */
@Data
public class PmsAttrGroupQueryDTO {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

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
