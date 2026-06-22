package cn.org.starpivot.system.controller.internal;

import cn.org.starpivot.api.system.dto.SysUserAuthDto;
import cn.org.starpivot.api.system.dto.VerifyPasswordRequest;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.system.domain.entity.SysMenu;
import cn.org.starpivot.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 内部用户服务控制器。
 * <p>
 * 供 auth 服务通过 Feign 调用的用户认证相关接口，路径 {@code /internal/user}，
 * 不经网关对外暴露。
 * </p>
 * <ul>
 *   <li>{@link Hidden} — 从 OpenAPI 文档中隐藏</li>
 *   <li>{@link RestController} — REST 控制器</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /internal/user}</li>
 * </ul>
 *
 * @see SysUserService
 */
@Hidden
@RestController
@RequestMapping("/internal/user")
@RequiredArgsConstructor
public class SysUserInternalController {

    private final SysUserService sysUserService;

    /**
     * 根据用户名查询认证用用户信息（含密码哈希、角色等）。
     *
     * @param username 登录名
     * @return 用户认证 DTO；不存在时返回 404
     */
    @GetMapping("/username/{username}")
    public Result<SysUserAuthDto> getByUsername(@PathVariable String username) {
        SysUserAuthDto user = sysUserService.getAuthByUsername(username);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 校验用户名与明文密码是否匹配。
     *
     * @param request 含用户名与密码的校验请求
     * @return 匹配成功返回用户认证 DTO，失败返回 401
     */
    @PostMapping("/verify-password")
    public Result<SysUserAuthDto> verifyPassword(@RequestBody VerifyPasswordRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return Result.unauthorized("用户名或密码错误");
        }
        SysUserAuthDto user = sysUserService.verifyPassword(request.getUsername(), request.getPassword());
        if (user == null) {
            return Result.unauthorized("用户名或密码错误");
        }
        return Result.success(user);
    }

    /**
     * 获取用户可访问的菜单列表（用于权限加载）。
     *
     * @param userId 用户主键
     * @return 菜单实体列表
     */
    @GetMapping("/{userId}/menus")
    public Result<List<SysMenu>> getUserMenus(@PathVariable Long userId) {
        List<SysMenu> menus = sysUserService.getUserMenus(userId);
        return Result.success(menus);
    }
}
