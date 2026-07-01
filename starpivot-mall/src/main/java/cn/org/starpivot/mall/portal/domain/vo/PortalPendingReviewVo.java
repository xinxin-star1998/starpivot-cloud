package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

@Data
public class PortalPendingReviewVo {

    private Long spuId;

    private Long skuId;

    private String spuName;

    private String coverImg;

    private String orderSn;
}
