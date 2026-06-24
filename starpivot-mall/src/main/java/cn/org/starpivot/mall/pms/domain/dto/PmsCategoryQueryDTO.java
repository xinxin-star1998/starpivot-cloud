package cn.org.starpivot.mall.pms.domain.dto;

import lombok.Data;

/**
 * 商品分类树形查询 DTO。
 */
@Data
public class PmsCategoryQueryDTO {

    /** 分类名称（模糊匹配） */
    private String name;

    /** 显示状态 */
    private Integer showStatus;
}
