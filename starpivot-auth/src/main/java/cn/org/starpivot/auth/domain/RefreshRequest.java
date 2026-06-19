package cn.org.starpivot.auth.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
