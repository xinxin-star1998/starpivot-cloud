package cn.org.starpivot.auth.domain;

import lombok.Builder;
import lombok.Data;

/**
 * 验证码生成响应 DTO。
 * <p>
 * 封装图形验证码的令牌与 Base64 编码图片，供前端展示。
 * </p>
 * <ul>
 *   <li>{@link Data} — Lombok 自动生成 getter/setter、{@code equals}、{@code hashCode}、{@code toString}</li>
 *   <li>{@link Builder} — Lombok 生成建造者模式，便于 {@link cn.org.starpivot.auth.service.CaptchaService} 组装响应</li>
 * </ul>
 */
@Data
@Builder
public class CaptchaResponse {

    /** 验证码唯一令牌，校验时需回传 */
    private String captchaToken;

    /** Base64 编码的 PNG 图片，格式为 {@code data:image/png;base64,...} */
    private String captchaImage;
}
