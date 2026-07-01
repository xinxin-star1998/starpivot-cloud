package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.system.domain.bo.RouterVo;
import cn.org.starpivot.system.domain.entity.SysMenu;
import cn.org.starpivot.system.service.SysMenuService;
import cn.org.starpivot.system.service.SysRouterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前端路由控制器：菜单树与 Vue 动态路由。
 */
@RestController
@RequestMapping("/router")
@RequiredArgsConstructor
@Tag(name = "路由管理", description = "动态路由、用户菜单树等接口")
public class RouterController {

    private final SysMenuService sysMenuService;
    private final SysRouterService sysRouterService;

    @Operation(summary = "获取用户菜单树")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/userMenuTree")
    public Result<List<SysMenu>> getUserMenuTree() {
        Long userId = requireUserId();
        return Result.success(sysMenuService.getUserMenuTree(userId));
    }

    @Operation(summary = "获取动态路由")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/dynamic-routes")
    public Result<List<RouterVo>> getDynamicRoutes() {
        Long userId = SecurityContextUtils.getUserId();
        if (userId == null) {
            return Result.success(List.of());
        }
        return Result.success(sysRouterService.buildDynamicRoutes(userId));
    }

    private Long requireUserId() {
        Long userId = SecurityContextUtils.getUserId();
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户未认证");
        }
        return userId;
    }
}
