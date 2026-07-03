package cn.org.starpivot.mall.portal.auth.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信小程序 wx.login 登录。
 */
@Data
public class PortalWechatMiniLoginBo {

    @NotBlank(message = "code 不能为空")
    private String code;
}
