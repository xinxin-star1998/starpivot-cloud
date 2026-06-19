package cn.org.starpivot.common.entity;

import lombok.Data;

/**
 * 树结构实体基类
 * 
 * @author StarPivot
 */
@Data
public class TreeEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 父菜单名称 */
    private String parentName;

    /** 父菜单ID */
    private Long parentId;

    /** 显示顺序 */
    private Integer orderNum;

    /** 祖先节点路径，逗号分隔 */
    private String ancestors;
}