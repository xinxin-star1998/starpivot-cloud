package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

@Data
public class PortalMemberVo {

    private Long id;

    private String username;

    private String nickname;

    private String mobile;

    private String header;

    private Integer integration;

    private Integer growth;

    private Long levelId;
}
