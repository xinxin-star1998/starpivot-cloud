package cn.org.starpivot.auth.domain;

import lombok.Builder;
import lombok.Data;

/**
 * 用户注册响应 DTO。
 * <p>
 * 封装注册成功后返回的新用户基本信息。
 * </p>
 * <ul>
 *   <li>{@link Data} — Lombok 自动生成 getter/setter、{@code equals}、{@code hashCode}、{@code toString}</li>
 *   <li>{@link Builder} — Lombok 生成建造者模式，便于服务层组装响应</li>
 * </ul>
 */
@Data
@Builder
public class RegisterResponse {

    /** 新注册用户的 ID */
    private Long userId;

    /** 注册用户名 */
    private String username;

    /** 用户昵称，由 system 服务分配 */
    private String nickName;
}
