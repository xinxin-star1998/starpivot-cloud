package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.system.domain.bo.MetaVo;
import cn.org.starpivot.system.domain.bo.RouterVo;
import cn.org.starpivot.system.domain.entity.SysMenu;
import cn.org.starpivot.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 前端路由控制器。
 * <p>
 * 根据当前登录用户权限生成菜单树与 Vue Router 动态路由配置。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /router}</li>
 *   <li>{@link Tag} — OpenAPI 分组「路由管理」</li>
 * </ul>
 */
@RestController
@RequestMapping("/router")
@RequiredArgsConstructor
@Tag(name = "路由管理", description = "动态路由、用户菜单树等接口")
public class RouterController {

    private final SysMenuService sysMenuService;

    /**
     * 获取当前用户的菜单树（原始 {@link SysMenu} 结构）。
     *
     * @param authentication Spring Security 认证对象
     * @return 菜单树；未认证返回 401，用户不存在返回 404
     */
    @Operation(summary = "获取用户菜单树")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/userMenuTree")
    public ResponseEntity<Result<List<SysMenu>>> getUserMenuTree(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, "用户未认证"));
        }
        List<SysMenu> menuTree = getCurrentUserMenuTree(authentication);
        if (menuTree == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(404, "用户不存在"));
        }
        return ResponseEntity.ok(Result.success(menuTree));
    }

    /**
     * 获取当前用户的 Vue 动态路由列表。
     *
     * @param authentication Spring Security 认证对象
     * @return {@link RouterVo} 路由树，无菜单时返回空列表
     */
    @Operation(summary = "获取动态路由")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/dynamic-routes")
    public Result<List<RouterVo>> getDynamicRoutes(Authentication authentication) {
        List<SysMenu> menuTree = getCurrentUserMenuTree(authentication);
        if (menuTree == null || menuTree.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        return Result.success(buildRouterTree(menuTree));
    }

    private List<SysMenu> getCurrentUserMenuTree(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Long userId = SecurityContextUtils.getUserId();
        if (userId == null) {
            return null;
        }
        return sysMenuService.getUserMenuTree(userId);
    }

    private List<RouterVo> buildRouterTree(List<SysMenu> menuTree) {
        List<RouterVo> routerTree = new ArrayList<>();
        for (SysMenu menu : menuTree) {
            RouterVo router = convertMenuToRouter(menu);
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                router.setChildren(buildRouterTree(menu.getChildren()));
            }
            routerTree.add(router);
        }
        return routerTree;
    }

    private RouterVo convertMenuToRouter(SysMenu menu) {
        RouterVo router = new RouterVo();
        router.setMenuId(menu.getMenuId());
        router.setPerms(menu.getPerms());
        router.setMenuType(menu.getMenuType());
        router.setIsFrame(menu.getIsFrame());
        router.setName(menu.getRouteName());
        router.setPath(menu.getPath());
        router.setHidden("1".equals(menu.getVisible()));
        router.setComponent(menu.getComponent());
        router.setQuery(menu.getQuery());

        MetaVo meta = new MetaVo();
        meta.setTitle(menu.getMenuName());
        meta.setIcon(menu.getIcon());
        meta.setNoCache(menu.getIsCache() != null && menu.getIsCache() == 1);

        if (menu.getIsFrame() != null && menu.getIsFrame() == 0 && menu.getPath() != null
                && (menu.getPath().startsWith("http://") || menu.getPath().startsWith("https://"))) {
            meta.setLink(menu.getPath());
        }

        router.setMeta(meta);
        return router;
    }
}
