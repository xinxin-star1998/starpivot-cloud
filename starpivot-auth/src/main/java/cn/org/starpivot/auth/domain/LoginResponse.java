package cn.org.starpivot.auth.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "登录响应（与前端 star-pivot-ui 对齐）")
public class LoginResponse {

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "刷新令牌")
    private String refreshToken;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "有效期（秒）")
    private Long expiresIn;
}
