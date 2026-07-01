package cn.org.starpivot.auth.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "登录请求")
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "admin")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码令牌（GET /captcha 返回）")
    private String captchaToken;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "用户输入的验证码")
    private String captcha;
}
