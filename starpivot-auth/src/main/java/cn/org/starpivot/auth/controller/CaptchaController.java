package cn.org.starpivot.auth.controller;

import cn.org.starpivot.auth.domain.CaptchaResponse;
import cn.org.starpivot.auth.service.CaptchaService;
import cn.org.starpivot.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 验证码控制器：提供图形验证码生成接口，支持按业务场景（scene）隔离。
 */
@Tag(name = "验证码")
@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    @Operation(summary = "获取验证码")
    @GetMapping
    public Result<CaptchaResponse> captcha(@RequestParam(defaultValue = "login") String scene) {
        return Result.success(captchaService.generate(scene));
    }
}
