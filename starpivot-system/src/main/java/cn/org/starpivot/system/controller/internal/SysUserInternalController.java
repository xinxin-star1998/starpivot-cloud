package cn.org.starpivot.system.controller.internal;

import cn.org.starpivot.api.system.dto.SysUserAuthDto;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.system.domain.entity.SysMenu;
import cn.org.starpivot.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 内部用户接口，仅供 auth 等服务 Feign 调用（不经网关暴露）
 */
@Hidden
@RestController
@RequestMapping("/internal/user")
@RequiredArgsConstructor
public class SysUserInternalController {

    private final SysUserService sysUserService;

    @GetMapping("/username/{username}")
    public Result<SysUserAuthDto> getByUsername(@PathVariable String username) {
        SysUserAuthDto user = sysUserService.getAuthByUsername(username);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        return Result.success(user);
    }

    @GetMapping("/{userId}/menus")
    public Result<List<SysMenu>> getUserMenus(@PathVariable Long userId) {
        List<SysMenu> menus = sysUserService.getUserMenus(userId);
        return Result.success(menus);
    }
}
