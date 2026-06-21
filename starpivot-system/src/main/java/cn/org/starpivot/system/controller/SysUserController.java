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

@Slf4j
@RestController
@RequestMapping("/sys/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户信息的增删改查、密码重置、状态修改、账户解锁等接口")
public class SysUserController {

    private final SysUserService sysUserService;
    private final AccountLockService accountLockService;
    private final SysUserExcelHandler sysUserExcelHandler;

    @Operation(summary = "分页查询用户")
    @PreAuthorize("hasAuthority('system:user:query')")
    @PostMapping("/pageList")
    public Result<PageResponse<UserVO>> pageList(@RequestBody UserReqBo userReqBo) {
        return Result.success(sysUserService.pageList(userReqBo));
    }

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

    @Log(title = "新增用户", businessType = BusinessType.INSERT)
    @Operation(summary = "新增用户")
    @PreAuthorize("hasAuthority('system:user:add')")
    @PostMapping("/add")
    public Result<Void> addUser(@Valid @RequestBody UserDTO userDTO) {
        return sysUserService.addUser(userDTO) ? Result.success("新增用户成功", null) : Result.error("新增用户失败");
    }

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

    @Log(title = "删除用户", businessType = BusinessType.DELETE)
    @Operation(summary = "删除用户")
    @PreAuthorize("hasAuthority('system:user:delete')")
    @DeleteMapping("/delete")
    public Result<Void> remove(@RequestBody DeleteRequest deleteRequest) {
        String errorMsg = sysUserService.canDeleteUsers(deleteRequest.getIds());
        if (errorMsg != null) {
            return Result.error(errorMsg);
        }
        return sysUserService.deleteUserByIds(deleteRequest.getIds())
                ? Result.success("删除用户成功", null) : Result.error("删除用户失败");
    }

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

    @Log(title = "修改用户状态", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改用户状态")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @PostMapping("/changeStatus")
    public Result<Void> changeStatus(@RequestBody UserDTO userDTO) {
        return sysUserService.changeUserStatus(userDTO.getUserId(), userDTO.getStatus())
                ? Result.success("修改状态成功", null) : Result.error("修改状态失败");
    }

    @Log(title = "解锁账户", businessType = BusinessType.UPDATE)
    @Operation(summary = "解锁账户")
    @PreAuthorize("hasAuthority('system:user:unLock') and @ss.hasRole('admin')")
    @PostMapping("/unlock/{userId}")
    public Result<Void> unlockUser(@PathVariable Long userId) {
        AccountLockService.UnlockResult r = accountLockService.unlockUserByUserId(userId);
        return r.isSuccess() ? Result.success(r.getMessage(), null) : Result.error(r.getMessage());
    }

    @Log(title = "导出用户", businessType = BusinessType.EXPORT)
    @PreAuthorize("hasAuthority('system:user:export')")
    @NoResponseWrapper
    @PostMapping("/export")
    public ResponseEntity<?> exportUsers(@RequestBody(required = false) UserReqBo userReqBo) {
        return ExcelToolkit.export(sysUserExcelHandler, userReqBo != null ? userReqBo : new UserReqBo(), SysUserExcel.class);
    }

    @Log(title = "导入用户", businessType = BusinessType.IMPORT)
    @PreAuthorize("hasAuthority('system:user:import')")
    @PostMapping("/import")
    public Result<ExcelImportResult> importUsers(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", defaultValue = "false") boolean updateSupport) throws Exception {
        return ExcelToolkit.importFile(file, sysUserExcelHandler, ExcelImportOptions.of(updateSupport), SysUserExcel.class);
    }

    @PreAuthorize("hasAuthority('system:user:import')")
    @NoResponseWrapper
    @GetMapping("/importTemplate")
    public ResponseEntity<?> importTemplate() {
        return ExcelToolkit.downloadTemplate(sysUserExcelHandler, new UserReqBo(), SysUserExcel.class);
    }
}
