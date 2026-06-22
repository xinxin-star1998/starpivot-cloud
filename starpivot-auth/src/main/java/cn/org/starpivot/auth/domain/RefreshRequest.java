package cn.org.starpivot.auth.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 令牌刷新请求 DTO。
 * <p>
 * 封装刷新访问令牌接口的请求参数，供 {@link cn.org.starpivot.auth.controller.AuthController#refresh} 使用。
 * </p>
 * <ul>
 *   <li>{@link Data} — Lombok 自动生成 getter/setter、{@code equals}、{@code hashCode}、{@code toString}</li>
 * </ul>
 */
@Data
public class RefreshRequest {

    /** 用户名，用于定位 Redis 中的刷新令牌 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 登录时下发的刷新令牌 */
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
