package cn.org.starpivot.mall.pms.domain.vo;

import lombok.Data;

@Data
public class BrandVo {

    private Long brandId;
    private String name;
    private String logo;
    private String descript;
    private Integer sort;
    private Integer showStatus;
    private String firstLetter;
}
