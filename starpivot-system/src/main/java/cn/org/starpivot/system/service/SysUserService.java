package cn.org.starpivot.system.service;

import cn.org.starpivot.api.system.dto.RegisterUserRequest;
import cn.org.starpivot.api.system.dto.RegisterUserResponse;
import cn.org.starpivot.api.system.dto.SysUserAuthDto;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.excel.ExcelImportResult;
import cn.org.starpivot.system.domain.bo.UserReqBo;
import cn.org.starpivot.system.domain.bo.UserVO;
import cn.org.starpivot.system.domain.dto.AssignUserReqBo;
import cn.org.starpivot.system.domain.dto.UserDTO;
import cn.org.starpivot.system.domain.entity.SysMenu;
import cn.org.starpivot.system.domain.entity.SysRole;
import cn.org.starpivot.system.domain.entity.SysUser;
import cn.org.starpivot.system.domain.excel.SysUserExcel;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SysUserService extends IService<SysUser> {

    SysUserAuthDto getAuthByUsername(String username);

    SysUserAuthDto verifyPassword(String username, String rawPassword);

    List<SysMenu> getUserMenus(Long userId);

    PageResponse<UserVO> pageList(UserReqBo userReqBo);

    SysUser getUserByUsername(String username);

    List<SysRole> getRolesByUserId(Long userId);

    SysUser getUserWithRoles(Long userId);

    List<SysMenu> getMenuByUserId(Long userId);

    boolean addUser(UserDTO userDTO);

    UserVO selectByUserId(Long userId);

    boolean updateUser(UserDTO userDTO);

    boolean changeUserStatus(Long userId, String status);

    boolean resetUserPassword(Long userId, String password);

    boolean updateUserPassword(Long userId, String oldPassword, String newPassword);

    boolean deleteUserByIds(List<Long> userIds);

    PageResponse<SysUser> getUserListByRoleId(AssignUserReqBo assignUserReqBo);

    PageResponse<SysUser> unallocatedList(AssignUserReqBo assignUserReqBo);

    List<SysUserExcel> listForExport(UserReqBo userReqBo);

    ExcelImportResult importFromExcel(List<SysUserExcel> rows, boolean updateSupport);

    boolean isCurrentUserSuperAdmin();

    boolean canUpdateUser(Long targetUserId);

    String canDeleteUsers(List<Long> userIds);

    String canResetPassword(Long targetUserId);

    List<Map<String, Object>> countByMonthRange(LocalDateTime start, LocalDateTime end);

    RegisterUserResponse registerUser(RegisterUserRequest request);
}
