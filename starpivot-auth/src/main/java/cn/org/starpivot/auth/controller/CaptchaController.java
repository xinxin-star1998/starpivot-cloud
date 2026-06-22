package cn.org.starpivot.auth.controller;

import cn.org.starpivot.auth.domain.CaptchaResponse;
import cn.org.starpivot.auth.domain.CaptchaVerifyRequest;
import cn.org.starpivot.auth.domain.CaptchaVerifyResponse;
import cn.org.starpivot.auth.service.CaptchaService;
import cn.org.starpivot.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 验证码控制器。
 * <p>
 * 提供图形验证码的生成与校验接口，支持按业务场景（scene）隔离验证码。
 * </p>
 * <ul>
 *   <li>{@link Tag} — OpenAPI 文档分组，名称为「验证码」</li>
 *   <li>{@link RestController} — 声明 REST 控制器</li>
 *   <li>{@link RequestMapping} — 统一前缀 {@code /captcha}</li>
 *   <li>{@link RequiredArgsConstructor} — Lombok 生成含 {@code final} 字段的构造器，注入 {@link CaptchaService}</li>
 * </ul>
 */
@Tag(name = "验证码")
@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    /**
     * 获取图形验证码。
     *
     * @param scene 业务场景标识，默认为 {@code login}
     * @return 含验证码令牌及 Base64 图片的 {@link Result}
     */
    @Operation(summary = "获取验证码")
    @GetMapping
    public Result<CaptchaResponse> captcha(@RequestParam(defaultValue = "login") String scene) {
        return Result.success(captchaService.generate(scene));
    }

    /**
     * 校验用户输入的验证码。
     *
     * @param request 校验请求体，包含验证码令牌、用户输入及场景
     * @return 校验通过后返回验证码凭证（captchaProof）的 {@link Result}
     */
    @Operation(summary = "校验验证码")
    @PostMapping("/verify")
    public Result<CaptchaVerifyResponse> verify(@RequestBody CaptchaVerifyRequest request) {
        return Result.success(captchaService.verify(request));
    }
}
