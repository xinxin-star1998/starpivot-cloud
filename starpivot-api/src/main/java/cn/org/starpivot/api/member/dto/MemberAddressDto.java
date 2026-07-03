package cn.org.starpivot.api.member.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MemberAddressDto implements Serializable {

    private Long id;
    private Long memberId;
    private String name;
    private String phone;
    private String postCode;
    private String province;
    private String city;
    private String region;
    private String detailAddress;
    private Integer defaultStatus;
}
