package cn.org.starpivot.mall.pms.domain.dto;

import lombok.Data;

/**
 * 商品属性DTO
 * 
 * @author admin
 * @since 2026-05-18
 */
@Data
public class PmsAttrDTO {

    /**
     * 属性id
     */
    private Long attrId;

    /**
     * 属性名
     */
    private String attrName;

    /**
     * 是否需要检索[0-不需要，1-需要]
     */
    private Integer searchType;

    /**
     * 值类型[0-为单个值，1-可以选择多个值]
     */
    private Integer valueType;

    /**
     * 属性图标
     */
    private String icon;

    /**
     * 可选值列表[用逗号分隔]
     */
    private String valueSelect;

    /**
     * 属性类型[0-销售属性，1-基本属性
     */
    private Integer attrType;

    /**
     * 启用状态[0 - 禁用，1 - 启用]
     */
    private Long enable;

    /**
     * 所属分类
     */
    private Long catelogId;

    /**
     * 所属属性分组 id。
     * 持久化在 {@code pms_attr_attrgroup_relation}，非 pms_attr 表字段。
     */
    private Long attrGroupId;

    /**
     * 属性在分组内的排序（关联表 attr_sort）。
     * 未传时服务端默认 0。
     */
    private Integer attrSort;

    /**
     * 快速展示【是否展示在介绍上；0-否 1-是】，在sku中仍然可以调整
     */
    private Integer showDesc;

}
