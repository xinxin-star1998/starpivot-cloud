package cn.org.starpivot.auth.controller;

import cn.org.starpivot.auth.domain.LoginRequest;
import cn.org.starpivot.auth.domain.LoginResponse;
import cn.org.starpivot.auth.domain.RefreshRequest;
import cn.org.starpivot.auth.domain.RegisterRequest;
import cn.org.starpivot.auth.domain.RegisterResponse;
import cn.org.starpivot.auth.domain.UserInfoResponse;
import cn.org.starpivot.auth.service.AuthService;
import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.security.JwtUtils;
import cn.org.starpivot.common.security.SecurityConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证授权")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(AppConstants.LOGIN_SUCCESS, authService.login(request));
    }

    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return Result.success(authService.refreshToken(request));
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success("注册成功", authService.register(request));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = JwtUtils.resolveToken(request.getHeader(SecurityConstants.TOKEN_HEADER));
        authService.logout(token);
        return Result.success(AppConstants.LOGOUT_SUCCESS, null);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping({"/user/info", "/userinfo"})
    public Result<UserInfoResponse> userInfo(HttpServletRequest request) {
        String token = JwtUtils.resolveToken(request.getHeader(SecurityConstants.TOKEN_HEADER));
        return Result.success(authService.getUserInfo(token));
    }
}
