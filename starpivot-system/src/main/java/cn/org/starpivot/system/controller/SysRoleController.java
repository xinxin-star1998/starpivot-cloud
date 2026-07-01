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

/**
 * 角色管理控制器。
 * <p>
 * 提供角色 CRUD、状态变更、菜单/数据权限分配及用户授权等 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /sys/role}</li>
 *   <li>{@link Tag} — OpenAPI 分组「角色管理」</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 * </ul>
 */
@RestController
@RequestMapping("/sys/role")
@Tag(name = "角色管理", description = "角色的增删改查、权限分配、用户分配等接口")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;
    private final SysUserService sysUserService;

    /**
     * 分页查询角色列表。
     *
     * @param roleQueryDTO 分页及筛选条件
     * @return 角色实体分页结果
     */
    @Operation(summary = "分页查询角色")
    @PreAuthorize("hasAuthority('system:role:query')")
    @PostMapping("/rolePageList")
    public Result<PageResponse<SysRole>> list(@RequestBody RoleQueryDTO roleQueryDTO) {
        return Result.success(sysRoleService.selectRoleList(roleQueryDTO));
    }

    /**
     * 查询全部角色下拉列表。
     *
     * @return 角色简要列表
     */
    @Operation(summary = "查询角色下拉列表")
    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/select")
    public Result<List<SysRole>> select() {
        return Result.success(sysRoleService.selectAllRoles());
    }

    /**
     * 根据角色 ID 获取详情。
     *
     * @param roleId 角色主键
     * @return 角色实体
     */
    @Operation(summary = "获取角色详情")
    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/{roleId}")
    public Result<SysRole> getInfo(@Parameter(description = "角色ID") @PathVariable("roleId") Long roleId) {
        return Result.success(sysRoleService.selectRoleById(roleId));
    }

    /**
     * 新增角色。
     *
     * @param roleDTO 角色创建参数
     * @return 操作结果
     */
    @Log(title = "新增角色", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('system:role:add')")
    @PostMapping("/addRole")
    public Result<?> add(@Valid @RequestBody RoleDTO roleDTO) {
        boolean success = sysRoleService.insertRole(roleDTO);
        return success ? Result.success("新增角色成功") : Result.error("新增角色失败");
    }

    /**
     * 修改角色信息。
     *
     * @param roleDTO 角色更新参数
     * @return 操作结果
     */
    @Log(title = "修改角色", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('system:role:edit')")
    @PutMapping("/updateRole")
    public Result<?> edit(@Valid @RequestBody RoleDTO roleDTO) {
        boolean success = sysRoleService.updateRole(roleDTO);
        return success ? Result.success("修改角色成功") : Result.error("修改角色失败");
    }

    /**
     * 批量删除角色。
     *
     * @param deleteRequest 待删除角色 ID 列表
     * @return 操作结果
     */
    @Log(title = "删除角色", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('system:role:delete')")
    @DeleteMapping("/removeRole")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        boolean success = sysRoleService.deleteRoleByIds(validateIds(deleteRequest.getIds()));
        return success ? Result.success("删除角色成功") : Result.error("删除角色失败");
    }

    /**
     * 启用或停用角色。
     *
     * @param roleDTO 含角色 ID 与目标状态
     * @return 操作结果
     */
    @Log(title = "修改角色状态", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('system:role:edit')")
    @PutMapping("/changeStatus")
    public Result<?> changeStatus(@RequestBody RoleDTO roleDTO) {
        boolean success = sysRoleService.changeRoleStatus(roleDTO.getRoleId(), roleDTO.getStatus());
        return success ? Result.success("修改状态成功") : Result.error("修改状态失败");
    }

    /**
     * 为角色分配菜单与数据权限（部门范围）。
     *
     * @param rolePermissionAssignDTO 权限分配参数
     * @return 操作结果
     */
    @Log(title = "分配角色数据权限", businessType = BusinessType.GRANT)
    @PreAuthorize("hasAuthority('system:role:assignDataScope')")
    @PostMapping("/assignPermission")
    public Result<?> assignPermission(@RequestBody RolePermissionAssignDTO rolePermissionAssignDTO) {
        boolean success = sysRoleService.assignPermission(rolePermissionAssignDTO);
        return success ? Result.success("分配权限成功") : Result.error("分配权限失败");
    }

    /**
     * 查询角色已关联的部门 ID 列表（数据权限）。
     *
     * @param roleId 角色主键
     * @return 部门 ID 列表
     */
    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/{roleId}/deptIds")
    public Result<List<Long>> getDeptIds(@PathVariable("roleId") Long roleId) {
        return Result.success(sysRoleService.selectDeptIdsByRoleId(roleId));
    }

    /**
     * 查询角色已分配的菜单 ID 列表。
     *
     * @param roleId 角色主键
     * @return 菜单 ID 列表
     */
    @PreAuthorize("hasAuthority('system:role:query')")
    @GetMapping("/getMenuIdsByRoleId/{roleId}")
    public Result<List<Long>> getMenuIdsByRoleId(@PathVariable("roleId") Long roleId) {
        return Result.success("查询成功", sysRoleService.getMenuIdsByRoleId(roleId));
    }

    /**
     * 分页查询已分配指定角色的用户。
     *
     * @param assignUserReqBo 角色 ID 及分页条件
     * @return 用户分页结果
     */
    @PreAuthorize("hasAuthority('system:role:allocatedList')")
    @PostMapping("/allocatedList")
    public Result<PageResponse<SysUser>> getUserListByRoleId(@RequestBody AssignUserReqBo assignUserReqBo) {
        return Result.success("查询成功", sysUserService.getUserListByRoleId(assignUserReqBo));
    }

    /**
     * 分页查询未分配指定角色的用户。
     *
     * @param assignUserReqBo 角色 ID 及分页条件
     * @return 用户分页结果
     */
    @PreAuthorize("hasAuthority('system:role:unallocatedList')")
    @PostMapping("/unallocatedList")
    public Result<PageResponse<SysUser>> getUserListNotInByRoleId(@RequestBody AssignUserReqBo assignUserReqBo) {
        return Result.success("查询成功", sysUserService.unallocatedList(assignUserReqBo));
    }

    /**
     * 为角色批量分配用户。
     *
     * @param userRoleDTO 角色与用户 ID 列表
     * @return 操作结果
     */
    @Log(title = "分配角色用户", businessType = BusinessType.GRANT)
    @PreAuthorize("hasAuthority('system:role:assignUser')")
    @PostMapping("/assignUser")
    public Result<?> assignUser(@RequestBody UserRoleDTO userRoleDTO) {
        boolean success = sysRoleService.assignUser(userRoleDTO);
        return success ? Result.success("分配用户成功") : Result.error("分配用户失败");
    }

    /**
     * 取消角色对指定用户的授权。
     *
     * @param userRole 用户与角色关联信息
     * @return 操作结果
     */
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
