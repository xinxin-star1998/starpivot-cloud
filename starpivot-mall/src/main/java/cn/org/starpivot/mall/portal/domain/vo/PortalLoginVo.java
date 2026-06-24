package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

@Data
public class PortalLoginVo {

    private String token;

    private PortalMemberVo member;
}
