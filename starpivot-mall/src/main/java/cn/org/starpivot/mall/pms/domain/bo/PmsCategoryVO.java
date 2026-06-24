package cn.org.starpivot.mall.pms.domain.bo;

import lombok.Data;

import java.util.List;

/**
 * 商品分类树形展示 VO。
 */
@Data
public class PmsCategoryVO {

    private Long catId;

    private String name;

    private Long parentCid;

    private Integer catLevel;

    private Integer showStatus;

    private Integer sort;

    private String icon;

    private String productUnit;

    private Integer productCount;

    private List<PmsCategoryVO> children;
}
