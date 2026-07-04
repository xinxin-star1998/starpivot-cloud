package cn.org.starpivot.api.member.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MemberOrderReturnRewardRequest implements Serializable {

    private Long orderId;
    private String orderSn;
    private Long memberId;
    private Long skuId;
    private Integer skuCount;
    private Integer orderIntegration;
    private Integer orderGrowth;
}
