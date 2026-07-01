package cn.org.starpivot.mall.portal.auth.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PortalSetPasswordBo {

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度为6-32")
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String code;
}
