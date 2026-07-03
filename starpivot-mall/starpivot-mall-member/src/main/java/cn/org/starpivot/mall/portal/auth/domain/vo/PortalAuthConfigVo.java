package cn.org.starpivot.mall.portal.auth.domain.vo;

import lombok.Data;

@Data
public class PortalAuthConfigVo {

    private boolean passwordLogin = true;
    private boolean smsLogin = true;
    private boolean wechatLogin = false;
    private boolean miniProgramLogin = false;
    private boolean qqLogin = false;
    private boolean smsMockEnabled;
    private boolean captchaRequired = false;
}
