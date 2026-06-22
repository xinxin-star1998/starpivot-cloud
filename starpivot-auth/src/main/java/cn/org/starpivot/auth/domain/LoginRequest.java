package cn.org.starpivot.auth.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 DTO。
 * <p>
 * 封装用户登录接口的请求参数，供 {@link cn.org.starpivot.auth.controller.AuthController#login} 使用。
 * </p>
 * <ul>
 *   <li>{@link Data} — Lombok 自动生成 getter/setter、{@code equals}、{@code hashCode}、{@code toString}</li>
 *   <li>{@link Schema} — OpenAPI 文档描述，标注类及字段的 API 说明</li>
 * </ul>
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {

    /** 登录用户名 */
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "admin")
    private String username;

    /** 登录密码 */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;

    /** 验证码校验凭证，验证码开启时由前端在登录前获取 */
    @Schema(description = "验证码凭证（可选）")
    private String captchaProof;
}
