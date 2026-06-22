package cn.org.starpivot.auth.domain;

import lombok.Builder;
import lombok.Data;

/**
 * 验证码校验响应 DTO。
 * <p>
 * 校验通过后返回短期有效的验证码凭证，供后续登录等接口携带。
 * </p>
 * <ul>
 *   <li>{@link Data} — Lombok 自动生成 getter/setter、{@code equals}、{@code hashCode}、{@code toString}</li>
 *   <li>{@link Builder} — Lombok 生成建造者模式，便于 {@link cn.org.starpivot.auth.service.CaptchaService} 组装响应</li>
 * </ul>
 */
@Data
@Builder
public class CaptchaVerifyResponse {

    /** 验证码校验凭证，有效期内可用于登录等需验证码的场景 */
    private String captchaProof;
}
