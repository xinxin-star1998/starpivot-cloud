package cn.org.starpivot.auth.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "忘记密码请求")
public class ForgotPasswordRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6到20个字符之间")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{6,20}$",
            message = "密码长度须为6到20位，且同时包含字母和数字")
    @Schema(description = "新密码（须 6-20 位且同时包含字母和数字）")
    private String password;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码令牌")
    private String captchaToken;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "用户输入的验证码")
    private String captcha;
}
