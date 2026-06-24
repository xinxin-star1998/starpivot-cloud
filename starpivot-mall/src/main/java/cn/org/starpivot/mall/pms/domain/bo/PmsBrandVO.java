package cn.org.starpivot.mall.pms.domain.bo;

import lombok.Data;

/**
 * 品牌列表展示 VO。
 */
@Data
public class PmsBrandVO {

    private Long brandId;

    private String name;

    private String logo;

    private String descript;

    private Integer showStatus;

    private String firstLetter;

    private Integer sort;
}
