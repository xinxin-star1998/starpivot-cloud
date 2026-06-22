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

/**
 * 认证授权控制器。
 * <p>
 * 对外暴露登录、注册、令牌刷新、登出及当前用户信息查询等 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link Tag} — OpenAPI 文档分组，名称为「认证授权」</li>
 *   <li>{@link RestController} — 声明 REST 控制器，返回值直接序列化为 JSON</li>
 *   <li>{@link RequiredArgsConstructor} — Lombok 生成含 {@code final} 字段的构造器，注入 {@link AuthService}</li>
 * </ul>
 */
@Tag(name = "认证授权")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录。
     *
     * @param request 登录请求体，包含用户名与密码
     * @return 含访问令牌、刷新令牌及用户基本信息的 {@link Result}
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(AppConstants.LOGIN_SUCCESS, authService.login(request));
    }

    /**
     * 使用刷新令牌换取新的访问令牌。
     *
     * @param request 刷新请求体，包含用户名与刷新令牌
     * @return 含新访问令牌及刷新令牌的 {@link Result}
     */
    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return Result.success(authService.refreshToken(request));
    }

    /**
     * 用户自助注册。
     *
     * @param request 注册请求体，包含用户名与密码
     * @return 含新用户 ID 及用户名的 {@link Result}
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success("注册成功", authService.register(request));
    }

    /**
     * 退出登录，将当前访问令牌加入黑名单并撤销刷新令牌。
     *
     * @param request HTTP 请求，从请求头解析 Bearer Token
     * @return 操作结果
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = JwtUtils.resolveToken(request.getHeader(SecurityConstants.TOKEN_HEADER));
        authService.logout(token);
        return Result.success(AppConstants.LOGOUT_SUCCESS, null);
    }

    /**
     * 获取当前登录用户的详细信息、角色及菜单权限。
     *
     * @param request HTTP 请求，从请求头解析 Bearer Token
     * @return 含用户信息、角色列表及权限列表的 {@link Result}
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping({"/user/info", "/userinfo"})
    public Result<UserInfoResponse> userInfo(HttpServletRequest request) {
        String token = JwtUtils.resolveToken(request.getHeader(SecurityConstants.TOKEN_HEADER));
        return Result.success(authService.getUserInfo(token));
    }
}
