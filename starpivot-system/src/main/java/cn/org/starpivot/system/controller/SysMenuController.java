package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.system.domain.dto.MenuDTO;
import cn.org.starpivot.system.domain.entity.SysMenu;
import cn.org.starpivot.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器。
 * <p>
 * 提供系统菜单树查询、增删改及父级菜单查询等 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /sys/menu}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入 {@link SysMenuService}</li>
 *   <li>{@link Tag} — OpenAPI 分组「菜单管理」</li>
 * </ul>
 */
@RestController
@RequestMapping("/sys/menu")
@RequiredArgsConstructor
@Tag(name = "菜单管理", description = "菜单的增删改查、菜单树查询等接口")
public class SysMenuController {

    private final SysMenuService sysMenuService;

    /**
     * 查询完整菜单树（管理端）。
     *
     * @return 树形菜单列表
     */
    @Operation(summary = "查询菜单树")
    @PreAuthorize("hasAuthority('system:menu:query')")
    @GetMapping("/menuTree")
    public Result<List<SysMenu>> menuTree() {
        return Result.success(sysMenuService.menuTree());
    }

    /**
     * 新增菜单节点。
     *
     * @param menuDTO 菜单创建参数
     * @return 操作结果
     */
    @Log(title = "新增菜单", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('system:menu:add')")
    @PostMapping("/add")
    public Result<?> add(@Valid @RequestBody MenuDTO menuDTO) {
        boolean success = sysMenuService.insertMenu(menuDTO);
        return success ? Result.success("新增菜单成功") : Result.error("新增菜单失败");
    }

    /**
     * 修改菜单信息。
     *
     * @param menuDTO 菜单更新参数
     * @return 操作结果
     */
    @Log(title = "修改菜单", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('system:menu:edit')")
    @PutMapping
    public Result<?> edit(@Valid @RequestBody MenuDTO menuDTO) {
        boolean success = sysMenuService.updateMenu(menuDTO);
        return success ? Result.success("修改菜单成功") : Result.error("修改菜单失败");
    }

    /**
     * 批量删除菜单。
     *
     * @param deleteRequest 待删除菜单 ID 列表
     * @return 操作结果
     */
    @Log(title = "删除菜单", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('system:menu:delete')")
    @DeleteMapping("/removeMenu")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        boolean success = sysMenuService.deleteMenuByIds(validateIds(deleteRequest.getIds()));
        return success ? Result.success("删除菜单成功") : Result.error("删除菜单失败");
    }

    /**
     * 查询可作为父级的菜单列表。
     *
     * @return 父级候选菜单列表
     */
    @PreAuthorize("hasAuthority('system:menu:query')")
    @GetMapping("/getParent")
    public Result<List<SysMenu>> getParent() {
        return Result.success("查询成功", sysMenuService.getParent());
    }

    /**
     * 根据菜单 ID 获取详情。
     *
     * @param menuId 菜单主键
     * @return 菜单实体
     */
    @PreAuthorize("hasAuthority('system:menu:query')")
    @GetMapping("/getById/{menuId}")
    public Result<SysMenu> getById(@Parameter(description = "菜单ID") @PathVariable("menuId") Long menuId) {
        return Result.success("查询成功", sysMenuService.getById(menuId));
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
