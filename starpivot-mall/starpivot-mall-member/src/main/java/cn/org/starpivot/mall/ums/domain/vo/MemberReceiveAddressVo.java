package cn.org.starpivot.mall.ums.domain.vo;

import lombok.Data;

/**
 * 会员收货地址 B 端 VO。
 */
@Data
public class MemberReceiveAddressVo {

    private Long id;

    private Long memberId;

    private String memberUsername;

    private String memberNickname;

    private String name;

    private String phone;

    private String postCode;

    private String province;

    private String city;

    private String region;

    private String detailAddress;

    private String areacode;

    private Integer defaultStatus;

    private String defaultStatusLabel;
}
