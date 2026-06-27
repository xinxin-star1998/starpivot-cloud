package cn.org.starpivot.auth.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "忘记密码请求")
public class ForgotPasswordRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "新密码")
    private String password;

    @NotBlank(message = "验证码凭证不能为空")
    @Schema(description = "验证码校验凭证")
    private String captchaProof;
}
