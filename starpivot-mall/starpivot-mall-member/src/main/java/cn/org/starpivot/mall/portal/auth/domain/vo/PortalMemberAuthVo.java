package cn.org.starpivot.mall.portal.auth.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PortalMemberAuthVo {

    private Integer authType;
    private String authTypeLabel;
    private String identifier;
    private String maskedIdentifier;
    private LocalDateTime bindTime;
}
