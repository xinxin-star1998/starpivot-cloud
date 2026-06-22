package cn.org.starpivot.auth.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户注册请求 DTO。
 * <p>
 * 封装自助注册接口的请求参数，供 {@link cn.org.starpivot.auth.controller.AuthController#register} 使用。
 * </p>
 * <ul>
 *   <li>{@link Data} — Lombok 自动生成 getter/setter、{@code equals}、{@code hashCode}、{@code toString}</li>
 * </ul>
 */
@Data
public class RegisterRequest {

    /** 注册用户名，不可为空 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 注册密码，不可为空 */
    @NotBlank(message = "密码不能为空")
    private String password;
}
