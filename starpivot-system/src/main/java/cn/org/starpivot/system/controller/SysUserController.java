package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.annotation.NoResponseWrapper;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.excel.ExcelImportOptions;
import cn.org.starpivot.common.excel.ExcelImportResult;
import cn.org.starpivot.common.excel.ExcelToolkit;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.system.domain.bo.UserReqBo;
import cn.org.starpivot.system.domain.bo.UserVO;
import cn.org.starpivot.system.domain.dto.ResetPasswordDTO;
import cn.org.starpivot.system.domain.dto.UpdatePasswordDTO;
import cn.org.starpivot.system.domain.dto.UserDTO;
import cn.org.starpivot.system.domain.excel.SysUserExcel;
import cn.org.starpivot.system.excel.SysUserExcelHandler;
import cn.org.starpivot.system.service.AccountLockService;
import cn.org.starpivot.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户管理控制器。
 * <p>
 * 提供用户 CRUD、密码管理、状态变更、账户解锁及 Excel 导入导出等 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link Slf4j} — 日志记录</li>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /sys/user}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Tag} — OpenAPI 分组「用户管理」</li>
 * </ul>
 *
 * @see SysUserService
 * @see AccountLockService
 */
@Slf4j
@RestController
@RequestMapping("/sys/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户信息的增删改查、密码重置、状态修改、账户解锁等接口")
public class SysUserController {

    private final SysUserService sysUserService;
    private final AccountLockService accountLockService;
    private final SysUserExcelHandler sysUserExcelHandler;

    /**
     * 分页查询用户列表。
     *
     * @param userReqBo 分页及筛选条件
     * @return 用户视图分页结果
     */
    @Operation(summary = "分页查询用户")
    @PreAuthorize("hasAuthority('system:user:query')")
    @PostMapping("/userPageList")
    public Result<PageResponse<UserVO>> list(@RequestBody UserReqBo userReqBo) {
        return Result.success(sysUserService.pageList(userReqBo));
    }

    /**
     * 根据用户 ID 获取用户详情。
     *
     * @param userId 用户主键
     * @return 用户视图对象，不存在时返回错误提示
     */
    @Operation(summary = "获取用户详情")
    @PreAuthorize("hasAuthority('system:user:query')")
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(@Parameter(description = "用户ID") @PathVariable Long userId) {
        UserVO userVO = sysUserService.selectByUserId(userId);
        if (userVO == null) {
            return Result.error("用户不存在");
        }
        return Result.success(userVO);
    }

    /**
     * 新增系统用户。
     * <p>标注 {@link Log} 记录操作日志，业务类型为 {@link BusinessType#INSERT}。</p>
     *
     * @param userDTO 用户创建参数，经 {@link Valid} 校验
     * @return 操作结果
     */
    @Log(title = "新增用户", businessType = BusinessType.INSERT)
    @Operation(summary = "新增用户")
    @PreAuthorize("hasAuthority('system:user:add')")
    @PostMapping("/add")
    public Result<Void> addUser(@Valid @RequestBody UserDTO userDTO) {
        return sysUserService.addUser(userDTO) ? Result.success("新增用户成功", null) : Result.error("新增用户失败");
    }

    /**
     * 修改用户信息。
     * <p>
     * 权限表达式允许拥有 {@code system:user:update} 权限，或目标用户为当前用户本人
     * （通过 {@code @sysUserService.canUpdateUser} SpEL 判断）。
     * </p>
     *
     * @param userDTO 用户更新参数
     * @return 操作结果
     */
    @Log(title = "修改用户", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改用户")
    @PreAuthorize("hasAuthority('system:user:update') or @sysUserService.canUpdateUser(#userDTO.userId)")
    @PostMapping("/update")
    public Result<Void> updateUser(@Valid @RequestBody UserDTO userDTO) {
        if (!sysUserService.canUpdateUser(userDTO.getUserId())) {
            return Result.error("无权修改该用户信息");
        }
        return sysUserService.updateUser(userDTO) ? Result.success("修改用户成功", null) : Result.error("修改用户失败");
    }

    /**
     * 批量删除用户。
     *
     * @param deleteRequest 待删除用户 ID 列表
     * @return 操作结果，含业务校验失败原因
     */
    @Log(title = "删除用户", businessType = BusinessType.DELETE)
    @Operation(summary = "删除用户")
    @PreAuthorize("hasAuthority('system:user:delete')")
    @DeleteMapping("/removeUser")
    public Result<Void> remove(@RequestBody DeleteRequest deleteRequest) {
        String errorMsg = sysUserService.canDeleteUsers(deleteRequest.getIds());
        if (errorMsg != null) {
            return Result.error(errorMsg);
        }
        return sysUserService.deleteUserByIds(deleteRequest.getIds())
                ? Result.success("删除用户成功", null) : Result.error("删除用户失败");
    }

    /**
     * 管理员重置指定用户密码。
     *
     * @param resetPasswordDTO 含用户 ID 与新密码
     * @return 操作结果
     */
    @Log(title = "重置密码", businessType = BusinessType.UPDATE)
    @Operation(summary = "重置密码")
    @PreAuthorize("hasAuthority('system:user:resetPwd')")
    @PostMapping("/resetPwd")
    public Result<Void> resetPwd(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        String errorMsg = sysUserService.canResetPassword(resetPasswordDTO.getUserId());
        if (errorMsg != null) {
            return Result.error(errorMsg);
        }
        return sysUserService.resetUserPassword(resetPasswordDTO.getUserId(), resetPasswordDTO.getPassword())
                ? Result.success("重置密码成功", null) : Result.error("重置密码失败");
    }

    /**
     * 当前登录用户修改自己的密码。
     *
     * @param updatePasswordDTO 旧密码与新密码
     * @return 操作结果，成功后需重新登录
     */
    @Log(title = "修改当前用户密码", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改当前用户密码")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/updatePwd")
    public Result<Void> updatePwd(@Valid @RequestBody UpdatePasswordDTO updatePasswordDTO) {
        Long currentUserId = SecurityContextUtils.getUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }
        return sysUserService.updateUserPassword(currentUserId, updatePasswordDTO.getOldPassword(), updatePasswordDTO.getNewPassword())
                ? Result.success("修改密码成功，请重新登录", null) : Result.error("修改密码失败，请检查旧密码是否正确");
    }

    /**
     * 启用或停用用户账号。
     *
     * @param userDTO 含用户 ID 与目标状态
     * @return 操作结果
     */
    @Log(title = "修改用户状态", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改用户状态")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @PostMapping("/changeStatus")
    public Result<Void> changeStatus(@RequestBody UserDTO userDTO) {
        return sysUserService.changeUserStatus(userDTO.getUserId(), userDTO.getStatus())
                ? Result.success("修改状态成功", null) : Result.error("修改状态失败");
    }

    /**
     * 解锁因登录失败被锁定的账户（仅管理员角色可操作）。
     *
     * @param userId 待解锁用户 ID
     * @return 解锁结果消息
     */
    @Log(title = "解锁账户", businessType = BusinessType.UPDATE)
    @Operation(summary = "解锁账户")
    @PreAuthorize("hasAuthority('system:user:unLock') and @ss.hasRole('admin')")
    @PostMapping("/unlock/{userId}")
    public Result<Void> unlockUser(@PathVariable Long userId) {
        AccountLockService.UnlockResult r = accountLockService.unlockUserByUserId(userId);
        return r.isSuccess() ? Result.success(r.getMessage(), null) : Result.error(r.getMessage());
    }

    /**
     * 导出用户数据为 Excel 文件。
     * <p>{@link NoResponseWrapper} 表示直接返回文件流，不包装为统一 {@link Result}。</p>
     *
     * @param userReqBo 可选导出筛选条件
     * @return Excel 文件响应实体
     */
    @Log(title = "导出用户", businessType = BusinessType.EXPORT)
    @PreAuthorize("hasAuthority('system:user:export')")
    @NoResponseWrapper
    @PostMapping("/export")
    public ResponseEntity<?> exportUsers(@RequestBody(required = false) UserReqBo userReqBo) {
        return ExcelToolkit.export(sysUserExcelHandler, userReqBo != null ? userReqBo : new UserReqBo(), SysUserExcel.class);
    }

    /**
     * 从 Excel 批量导入用户。
     *
     * @param file           上传的 Excel 文件
     * @param updateSupport  是否允许更新已存在用户
     * @return 导入统计结果
     * @throws Exception 文件解析异常
     */
    @Log(title = "导入用户", businessType = BusinessType.IMPORT)
    @PreAuthorize("hasAuthority('system:user:import')")
    @PostMapping("/import")
    public Result<ExcelImportResult> importUsers(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", defaultValue = "false") boolean updateSupport) throws Exception {
        return ExcelToolkit.importFile(file, sysUserExcelHandler, ExcelImportOptions.of(updateSupport), SysUserExcel.class);
    }

    /**
     * 下载用户导入 Excel 模板。
     *
     * @return 模板文件响应实体
     */
    @PreAuthorize("hasAuthority('system:user:import')")
    @NoResponseWrapper
    @GetMapping("/importTemplate")
    public ResponseEntity<?> importTemplate() {
        return ExcelToolkit.downloadTemplate(sysUserExcelHandler, new UserReqBo(), SysUserExcel.class);
    }
}
