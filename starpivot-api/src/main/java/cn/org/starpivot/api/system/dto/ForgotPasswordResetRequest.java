package cn.org.starpivot.api.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 忘记密码重置请求（auth → system 内部调用）。
 */
@Data
public class ForgotPasswordResetRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
