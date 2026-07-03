package cn.org.starpivot.api.member.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MemberOrderIntegrationRequest implements Serializable {

    private Long memberId;
    private Long orderId;
    private String orderSn;
    private Integer useIntegration;
}
