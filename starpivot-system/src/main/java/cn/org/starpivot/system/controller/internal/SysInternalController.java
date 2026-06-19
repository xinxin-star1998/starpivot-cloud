package cn.org.starpivot.system.controller.internal;

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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Hidden
@RestController
@RequiredArgsConstructor
public class SysInternalController {

    private final SysConfigService sysConfigService;
    private final SysLogininforService sysLogininforService;
    private final SysOperLogService sysOperLogService;
    private final SysUserService sysUserService;

    @GetMapping("/internal/config/register-enabled")
    public Result<Boolean> isRegisterEnabled() {
        return Result.success(sysConfigService.isRegisterUserEnabled());
    }

    @PostMapping("/internal/logininfor")
    public Result<Void> saveLoginLog(@RequestBody LoginLogDto dto) {
        SysLogininfor logininfor = new SysLogininfor();
        BeanUtils.copyProperties(dto, logininfor);
        logininfor.setLoginTime(LocalDateTime.now());
        sysLogininforService.saveLogininfor(logininfor);
        return Result.success(null);
    }

    @PostMapping("/internal/user/register")
    public Result<RegisterUserResponse> registerUser(@RequestBody RegisterUserRequest request) {
        return Result.success(sysUserService.registerUser(request));
    }

    @DeleteMapping("/internal/operlog/clean")
    public Result<Void> cleanOperLog() {
        sysOperLogService.remove(null);
        return Result.success(null);
    }
}

