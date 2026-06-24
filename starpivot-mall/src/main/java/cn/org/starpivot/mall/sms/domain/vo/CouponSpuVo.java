package cn.org.starpivot.mall.sms.domain.vo;

import lombok.Data;

@Data
public class CouponSpuVo {

    private Long id;
    private Long couponId;
    private Long spuId;
    private String spuName;
}
