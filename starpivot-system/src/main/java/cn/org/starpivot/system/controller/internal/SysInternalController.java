package cn.org.starpivot.system.controller.internal;

import cn.org.starpivot.api.system.dto.ForgotPasswordResetRequest;
import cn.org.starpivot.api.system.dto.LoginLogDto;
import cn.org.starpivot.api.system.dto.RegisterUserRequest;
import cn.org.starpivot.api.system.dto.RegisterUserResponse;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.system.domain.entity.SysLogininfor;
import cn.org.starpivot.system.service.SysConfigService;
import cn.org.starpivot.system.service.SysLogininforService;
import cn.org.starpivot.system.service.SysOperLogService;
import cn.org.starpivot.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 系统模块内部服务控制器。
 * <p>
 * 供 auth 等微服务通过 Feign 调用的内部 REST 接口，路径以 {@code /internal} 开头，
 * 在 {@link cn.org.starpivot.system.config.SystemSecurityConfig} 中放行，不经网关对外暴露。
 * </p>
 * <ul>
 *   <li>{@link Hidden} — 从 OpenAPI 文档中隐藏</li>
 *   <li>{@link RestController} — REST 控制器</li>
 * </ul>
 */
@Hidden
@RestController
@RequiredArgsConstructor
public class SysInternalController {

    private final SysConfigService sysConfigService;
    private final SysLogininforService sysLogininforService;
    private final SysOperLogService sysOperLogService;
    private final SysUserService sysUserService;

    /**
     * 查询系统是否开放用户自助注册。
     *
     * @return 注册开关状态
     */
    @GetMapping("/internal/config/register-enabled")
    public Result<Boolean> isRegisterEnabled() {
        return Result.success(sysConfigService.isRegisterUserEnabled());
    }

    @GetMapping("/internal/config/forget-password-enabled")
    public Result<Boolean> isForgetPasswordEnabled() {
        return Result.success(sysConfigService.isForgetPasswordEnabled());
    }

    /**
     * 保存登录审计日志（由 auth 服务在登录成功/失败后回调）。
     *
     * @param dto 登录日志传输对象
     * @return 空成功响应
     */
    @PostMapping("/internal/logininfor")
    public Result<Void> saveLoginLog(@RequestBody LoginLogDto dto) {
        SysLogininfor logininfor = new SysLogininfor();
        BeanUtils.copyProperties(dto, logininfor);
        logininfor.setLoginTime(LocalDateTime.now());
        sysLogininforService.saveLogininfor(logininfor);
        return Result.success(null);
    }

    /**
     * 用户自助注册（内部接口，由 auth 服务调用）。
     *
     * @param request 注册请求参数
     * @return 注册结果（含新用户 ID）
     */
    @PostMapping("/internal/user/register")
    public Result<RegisterUserResponse> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        return Result.success(sysUserService.registerUser(request));
    }

    @PostMapping("/internal/user/forgot-password")
    public Result<Boolean> resetPasswordByForgot(@Valid @RequestBody ForgotPasswordResetRequest request) {
        boolean success = sysUserService.resetPasswordByForgot(request.getUsername(), request.getPassword());
        return Result.success(success);
    }

    /**
     * 清空全部操作日志（内部维护接口）。
     *
     * @return 空成功响应
     */
    @DeleteMapping("/internal/operlog/clean")
    public Result<Void> cleanOperLog() {
        sysOperLogService.remove(null);
        return Result.success(null);
    }
}
