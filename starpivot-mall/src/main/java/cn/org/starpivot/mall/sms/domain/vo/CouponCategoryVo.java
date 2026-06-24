package cn.org.starpivot.mall.sms.domain.vo;

import lombok.Data;

@Data
public class CouponCategoryVo {

    private Long id;
    private Long couponId;
    private Long categoryId;
    private String categoryName;
}
