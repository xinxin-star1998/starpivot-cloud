package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

@Data
public class PortalOrderSubmitVo {

    private Long orderId;

    private String orderSn;

    private Integer status;
}
