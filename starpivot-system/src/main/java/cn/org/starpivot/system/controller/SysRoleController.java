package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.system.domain.dto.*;
import cn.org.starpivot.system.domain.entity.SysRole;
import cn.org.starpivot.system.domain.entity.SysUser;
import cn.org.starpivot.system.domain.entity.UserRole;
import cn.org.starpivot.system.service.SysRoleService;
import cn.org.starpivot.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/role")
@Tag(name = "角色管理", description = "角色的增删改查、权限分配、用户分配等接口")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;
    private final SysUserService sysUserService;

    @Operation(summary = "分页查询角色")
    @PreAuthorize("hasAuthority('system:role:query')")
    @PostMapping("/list")
    public Result<PageResponse<SysRole>> list(@RequestBody RoleQueryDTO roleQueryDTO) {
        return Result.success(sysRoleService.selectRoleList(roleQueryDTO));
    }

    @Operation(summary = "查询角色下拉列表")
    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/select")
    public Result<List<SysRole>> select() {
        return Result.success(sysRoleService.selectAllRoles());
    }

    @Operation(summary = "获取角色详情")
    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/{roleId}")
    public Result<SysRole> getInfo(@Parameter(description = "角色ID") @PathVariable("roleId") Long roleId) {
        return Result.success(sysRoleService.selectRoleById(roleId));
    }

    @Log(title = "新增角色", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('system:role:add')")
    @PostMapping("/addRole")
    public Result<?> add(@Valid @RequestBody RoleDTO roleDTO) {
        boolean success = sysRoleService.insertRole(roleDTO);
        return success ? Result.success("新增角色成功") : Result.error("新增角色失败");
    }

    @Log(title = "修改角色", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('system:role:edit')")
    @PutMapping("updateRole")
    public Result<?> edit(@Valid @RequestBody RoleDTO roleDTO) {
        boolean success = sysRoleService.updateRole(roleDTO);
        return success ? Result.success("修改角色成功") : Result.error("修改角色失败");
    }

    @Log(title = "删除角色", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('system:role:delete')")
    @DeleteMapping("/delete")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        boolean success = sysRoleService.deleteRoleByIds(validateIds(deleteRequest.getIds()));
        return success ? Result.success("删除角色成功") : Result.error("删除角色失败");
    }

    @Log(title = "修改角色状态", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('system:role:edit')")
    @PutMapping("/changeStatus")
    public Result<?> changeStatus(@RequestBody RoleDTO roleDTO) {
        boolean success = sysRoleService.changeRoleStatus(roleDTO.getRoleId(), roleDTO.getStatus());
        return success ? Result.success("修改状态成功") : Result.error("修改状态失败");
    }

    @Log(title = "分配角色数据权限", businessType = BusinessType.GRANT)
    @PreAuthorize("hasAuthority('system:role:assignDataScope')")
    @PostMapping("/assignPermission")
    public Result<?> assignPermission(@RequestBody RolePermissionAssignDTO rolePermissionAssignDTO) {
        boolean success = sysRoleService.assignPermission(rolePermissionAssignDTO);
        return success ? Result.success("分配权限成功") : Result.error("分配权限失败");
    }

    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/{roleId}/deptIds")
    public Result<List<Long>> getDeptIds(@PathVariable("roleId") Long roleId) {
        return Result.success(sysRoleService.selectDeptIdsByRoleId(roleId));
    }

    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/getMenuIdsByRoleId/{roleId}")
    public Result<List<Long>> getMenuIdsByRoleId(@PathVariable("roleId") Long roleId) {
        return Result.success("查询成功", sysRoleService.getMenuIdsByRoleId(roleId));
    }

    @PreAuthorize("hasAuthority('system:role:allocatedList')")
    @PostMapping("/allocatedList")
    public Result<PageResponse<SysUser>> getUserListByRoleId(@RequestBody AssignUserReqBo assignUserReqBo) {
        return Result.success("查询成功", sysUserService.getUserListByRoleId(assignUserReqBo));
    }

    @PreAuthorize("hasAuthority('system:role:unallocatedList')")
    @PostMapping("/unallocatedList")
    public Result<PageResponse<SysUser>> getUserListNotInByRoleId(@RequestBody AssignUserReqBo assignUserReqBo) {
        return Result.success("查询成功", sysUserService.unallocatedList(assignUserReqBo));
    }

    @Log(title = "分配角色用户", businessType = BusinessType.GRANT)
    @PreAuthorize("hasAuthority('system:role:assignUser')")
    @PostMapping("/assignUser")
    public Result<?> assignUser(@RequestBody UserRoleDTO userRoleDTO) {
        boolean success = sysRoleService.assignUser(userRoleDTO);
        return success ? Result.success("分配用户成功") : Result.error("分配用户失败");
    }

    @Log(title = "取消角色用户授权", businessType = BusinessType.GRANT)
    @PreAuthorize("hasAuthority('system:role:cancelUser')")
    @DeleteMapping("/cancelUser")
    public Result<?> cancelUser(@RequestBody UserRole userRole) {
        boolean success = sysRoleService.cancelUser(userRole);
        return success ? Result.success("取消授权成功") : Result.error("取消授权失败");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
