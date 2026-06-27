package cn.org.starpivot.auth.controller;

import cn.org.starpivot.auth.domain.ForgotPasswordRequest;
import cn.org.starpivot.auth.service.AuthService;
import cn.org.starpivot.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "忘记密码")
@RestController
@RequestMapping("/forgot-password")
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final AuthService authService;

    @Operation(summary = "查询忘记密码开关")
    @GetMapping("/enabled")
    public Result<Map<String, Boolean>> enabled() {
        return Result.success(Map.of("forgetPasswordEnabled", authService.isForgetPasswordEnabled()));
    }

    @Operation(summary = "重置密码")
    @PostMapping
    public Result<Void> reset(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return Result.success("密码重置成功，请使用新密码登录", null);
    }
}
