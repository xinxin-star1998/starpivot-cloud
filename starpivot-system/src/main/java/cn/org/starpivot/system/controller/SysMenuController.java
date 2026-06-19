package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.domain.Result;
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

@RestController
@RequestMapping("/sys/menu")
@RequiredArgsConstructor
@Tag(name = "菜单管理", description = "菜单的增删改查、菜单树查询等接口")
public class SysMenuController {

    private final SysMenuService sysMenuService;

    @Operation(summary = "查询菜单树")
    @PreAuthorize("hasAuthority('system:menu:query')")
    @GetMapping("/menuTree")
    public Result<List<SysMenu>> menuTree() {
        return Result.success(sysMenuService.menuTree());
    }

    @Log(title = "新增菜单", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('system:menu:add')")
    @PostMapping("/add")
    public Result<?> add(@Valid @RequestBody MenuDTO menuDTO) {
        boolean success = sysMenuService.insertMenu(menuDTO);
        return success ? Result.success("新增菜单成功") : Result.error("新增菜单失败");
    }

    @Log(title = "修改菜单", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('system:menu:edit')")
    @PutMapping
    public Result<?> edit(@Valid @RequestBody MenuDTO menuDTO) {
        boolean success = sysMenuService.updateMenu(menuDTO);
        return success ? Result.success("修改菜单成功") : Result.error("修改菜单失败");
    }

    @Log(title = "删除菜单", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('system:menu:delete')")
    @DeleteMapping("/delete")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        boolean success = sysMenuService.deleteMenuByIds(validateIds(deleteRequest.getIds()));
        return success ? Result.success("删除菜单成功") : Result.error("删除菜单失败");
    }

    @PreAuthorize("hasAuthority('system:menu:query')")
    @GetMapping("/getParent")
    public Result<List<SysMenu>> getParent() {
        return Result.success("查询成功", sysMenuService.getParent());
    }

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
