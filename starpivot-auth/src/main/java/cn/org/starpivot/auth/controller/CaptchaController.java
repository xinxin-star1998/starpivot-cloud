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

    @Operation(summary = "校验验证码")
    @PostMapping("/verify")
    public Result<CaptchaVerifyResponse> verify(@RequestBody CaptchaVerifyRequest request) {
        return Result.success(captchaService.verify(request));
    }
}
