package cn.org.starpivot.auth.domain;

import lombok.Data;

/**
 * 验证码校验请求 DTO。
 * <p>
 * 封装验证码校验接口的请求参数，供 {@link cn.org.starpivot.auth.controller.CaptchaController#verify} 使用。
 * </p>
 * <ul>
 *   <li>{@link Data} — Lombok 自动生成 getter/setter、{@code equals}、{@code hashCode}、{@code toString}</li>
 * </ul>
 */
@Data
public class CaptchaVerifyRequest {

    /** 获取验证码时返回的令牌，用于 Redis 查找对应验证码 */
    private String captchaToken;

    /** 用户输入的验证码文本 */
    private String code;

    /** 业务场景标识，默认 {@code login}，需与获取验证码时的 scene 一致 */
    private String scene;
}
