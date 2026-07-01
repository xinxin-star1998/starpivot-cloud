package cn.org.starpivot.mall.portal.auth.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PortalUnbindAuthBo {

    @NotBlank(message = "验证码不能为空")
    private String code;
}
