package cn.org.starpivot.mall.portal.auth.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PortalWechatAuthorizeVo {

    private String authorizeUrl;
    private String state;
}
