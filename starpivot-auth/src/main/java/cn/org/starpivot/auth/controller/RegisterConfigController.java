package cn.org.starpivot.auth.controller;

import cn.org.starpivot.auth.service.AuthService;
import cn.org.starpivot.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "注册配置")
@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegisterConfigController {

    private final AuthService authService;

    @Operation(summary = "查询注册开关")
    @GetMapping("/enabled")
    public Result<Map<String, Boolean>> registerEnabled() {
        return Result.success(Map.of("registerEnabled", authService.isRegisterEnabled()));
    }
}
