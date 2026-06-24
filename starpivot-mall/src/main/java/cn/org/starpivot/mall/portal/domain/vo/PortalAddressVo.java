package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

@Data
public class PortalAddressVo {

    private Long id;

    private String name;

    private String phone;

    private String postCode;

    private String province;

    private String city;

    private String region;

    private String detailAddress;

    private Integer defaultStatus;
}
