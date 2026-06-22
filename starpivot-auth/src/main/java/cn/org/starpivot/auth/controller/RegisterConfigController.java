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

/**
 * 注册配置控制器。
 * <p>
 * 提供注册功能开关查询接口，供前端在展示注册入口前判断是否开放注册。
 * </p>
 * <ul>
 *   <li>{@link Tag} — OpenAPI 文档分组，名称为「注册配置」</li>
 *   <li>{@link RestController} — 声明 REST 控制器</li>
 *   <li>{@link RequestMapping} — 统一前缀 {@code /register}</li>
 *   <li>{@link RequiredArgsConstructor} — Lombok 生成含 {@code final} 字段的构造器，注入 {@link AuthService}</li>
 * </ul>
 */
@Tag(name = "注册配置")
@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegisterConfigController {

    private final AuthService authService;

    /**
     * 查询用户注册功能是否已开启。
     *
     * @return 含 {@code registerEnabled} 布尔值的 {@link Result}
     */
    @Operation(summary = "查询注册开关")
    @GetMapping("/enabled")
    public Result<Map<String, Boolean>> registerEnabled() {
        return Result.success(Map.of("registerEnabled", authService.isRegisterEnabled()));
    }
}
