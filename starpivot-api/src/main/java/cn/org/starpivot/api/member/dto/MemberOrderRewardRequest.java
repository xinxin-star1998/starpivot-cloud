package cn.org.starpivot.api.member.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MemberOrderRewardRequest implements Serializable {

    private Long orderId;
}
