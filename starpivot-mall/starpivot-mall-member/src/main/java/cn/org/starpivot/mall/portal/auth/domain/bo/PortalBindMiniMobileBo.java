package cn.org.starpivot.mall.portal.auth.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信小程序手机号快速验证组件 code。
 */
@Data
public class PortalBindMiniMobileBo {

    @NotBlank(message = "手机号授权码不能为空")
    private String code;
}
