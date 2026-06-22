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

/**
 * 系统用户服务接口。
 * <p>
 * 封装用户 CRUD、认证信息查询、角色/菜单关联、密码管理及 Excel 导入导出等业务。
 * 继承 {@link IService} 获得 MyBatis-Plus 基础持久化能力。
 * </p>
 */
public interface SysUserService extends IService<SysUser> {

    /** 根据用户名获取认证用用户信息（供 auth 内部调用）。 */
    SysUserAuthDto getAuthByUsername(String username);

    /** 校验用户名与明文密码，成功返回认证 DTO。 */
    SysUserAuthDto verifyPassword(String username, String rawPassword);

    /** 获取用户可访问的菜单列表。 */
    List<SysMenu> getUserMenus(Long userId);

    /** 分页查询用户列表。 */
    PageResponse<UserVO> pageList(UserReqBo userReqBo);

    /** 根据用户名查询用户实体。 */
    SysUser getUserByUsername(String username);

    /** 查询用户关联的角色列表。 */
    List<SysRole> getRolesByUserId(Long userId);

    /** 查询用户及其角色信息。 */
    SysUser getUserWithRoles(Long userId);

    /** 查询用户拥有的菜单权限列表。 */
    List<SysMenu> getMenuByUserId(Long userId);

    /** 新增用户（含角色、岗位关联）。 */
    boolean addUser(UserDTO userDTO);

    /** 根据用户 ID 查询用户视图对象。 */
    UserVO selectByUserId(Long userId);

    /** 更新用户信息。 */
    boolean updateUser(UserDTO userDTO);

    /** 修改用户启用/停用状态。 */
    boolean changeUserStatus(Long userId, String status);

    /** 管理员重置用户密码。 */
    boolean resetUserPassword(Long userId, String password);

    /** 用户修改自己的密码（需校验旧密码）。 */
    boolean updateUserPassword(Long userId, String oldPassword, String newPassword);

    /** 批量删除用户。 */
    boolean deleteUserByIds(List<Long> userIds);

    /** 分页查询已分配指定角色的用户。 */
    PageResponse<SysUser> getUserListByRoleId(AssignUserReqBo assignUserReqBo);

    /** 分页查询未分配指定角色的用户。 */
    PageResponse<SysUser> unallocatedList(AssignUserReqBo assignUserReqBo);

    /** 查询待导出的用户 Excel 行数据。 */
    List<SysUserExcel> listForExport(UserReqBo userReqBo);

    /** 从 Excel 行数据批量导入用户。 */
    ExcelImportResult importFromExcel(List<SysUserExcel> rows, boolean updateSupport);

    /** 判断当前登录用户是否为超级管理员。 */
    boolean isCurrentUserSuperAdmin();

    /** 判断当前用户是否有权修改目标用户。 */
    boolean canUpdateUser(Long targetUserId);

    /** 校验是否允许删除指定用户，返回错误消息或 null。 */
    String canDeleteUsers(List<Long> userIds);

    /** 校验是否允许重置目标用户密码，返回错误消息或 null。 */
    String canResetPassword(Long targetUserId);

    /** 按月份统计用户注册/新增数量（工作台图表用）。 */
    List<Map<String, Object>> countByMonthRange(LocalDateTime start, LocalDateTime end);

    /** 用户自助注册（内部接口）。 */
    RegisterUserResponse registerUser(RegisterUserRequest request);
}
