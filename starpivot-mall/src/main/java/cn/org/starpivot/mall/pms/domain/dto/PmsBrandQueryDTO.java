package cn.org.starpivot.mall.pms.domain.dto;

import lombok.Data;

/**
 * 品牌分页查询 DTO。
 */
@Data
public class PmsBrandQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    /** 品牌名称（模糊匹配） */
    private String name;

    /** 显示状态 */
    private Integer showStatus;

    /** 检索首字母 */
    private String firstLetter;
}
