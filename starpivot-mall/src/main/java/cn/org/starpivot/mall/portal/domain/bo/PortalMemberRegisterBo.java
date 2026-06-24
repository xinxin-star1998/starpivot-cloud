package cn.org.starpivot.mall.portal.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PortalMemberRegisterBo {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 32, message = "用户名长度为2-32")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度为6-32")
    private String password;

    private String nickname;

    private String mobile;
}
