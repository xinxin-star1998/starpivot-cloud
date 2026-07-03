package cn.org.starpivot.mall.sms.domain.vo;

import lombok.Data;

@Data
public class HomeCategoryHotVo {

    private Long id;

    private Long catId;

    /** 分类名称（来自 pms_category） */
    private String catName;

    private String title;

    private String icon;

    private String url;

    private Integer status;

    private Integer sort;

    private String note;
}
