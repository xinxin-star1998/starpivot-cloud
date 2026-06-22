package cn.org.starpivot.auth.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 登录响应 DTO。
 * <p>
 * 封装登录或令牌刷新成功后返回的访问令牌及用户基本信息，字段结构与前端 star-pivot-ui 对齐。
 * </p>
 * <ul>
 *   <li>{@link Data} — Lombok 自动生成 getter/setter、{@code equals}、{@code hashCode}、{@code toString}</li>
 *   <li>{@link Builder} — Lombok 生成建造者模式，便于 {@link cn.org.starpivot.auth.service.AuthService} 组装响应</li>
 *   <li>{@link Schema} — OpenAPI 文档描述</li>
 * </ul>
 */
@Data
@Builder
@Schema(description = "登录响应（与前端 star-pivot-ui 对齐）")
public class LoginResponse {

    /** JWT 访问令牌 */
    @Schema(description = "访问令牌")
    private String token;

    /** 刷新令牌，用于无感续期访问令牌 */
    @Schema(description = "刷新令牌")
    private String refreshToken;

    /** 用户名 */
    @Schema(description = "用户名")
    private String username;

    /** 用户昵称，无昵称时与用户名相同 */
    @Schema(description = "昵称")
    private String nickname;

    /** 访问令牌有效期，单位：秒 */
    @Schema(description = "有效期（秒）")
    private Long expiresIn;
}
