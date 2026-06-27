package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.api.system.dto.RegisterUserRequest;
import cn.org.starpivot.api.system.dto.RegisterUserResponse;
import cn.org.starpivot.api.system.dto.SysUserAuthDto;
import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.entity.DataScope;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.excel.ExcelImportResult;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.common.security.SecurityUtils;
import cn.org.starpivot.common.util.AssertUtils;
import cn.org.starpivot.system.assembler.UserVOAssembler;
import cn.org.starpivot.system.domain.bo.UserReqBo;
import cn.org.starpivot.system.domain.bo.UserVO;
import cn.org.starpivot.system.domain.dto.AssignUserReqBo;
import cn.org.starpivot.system.domain.dto.UserDTO;
import cn.org.starpivot.system.domain.entity.*;
import cn.org.starpivot.system.domain.excel.SysUserExcel;
import cn.org.starpivot.system.mapper.SysUserMapper;
import cn.org.starpivot.system.mapper.UserPostMapper;
import cn.org.starpivot.system.mapper.UserRoleMapper;
import cn.org.starpivot.system.service.DataScopeService;
import cn.org.starpivot.system.service.SysUserService;
import cn.org.starpivot.system.service.UserPermissionCacheService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 系统用户服务实现类。
 * <p>
 * 实现 {@link SysUserService}，涵盖用户 CRUD、认证查询、角色/岗位关联、
 * 数据权限过滤、密码管理及 Excel 导入导出等业务逻辑。
 * </p>
 * <ul>
 *   <li>{@link Slf4j} — 日志记录</li>
 *   <li>{@link Service} — Bean 名称 {@code sysUserService}，供 SpEL 引用</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 */
@Slf4j
@Service("sysUserService")
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserPostMapper userPostMapper;
    private final UserPermissionCacheService userPermissionCacheService;
    private final DataScopeService dataScopeService;
    private final UserVOAssembler userVOAssembler;
    private final TransactionTemplate transactionTemplate;
    private final SecurityUtils securityUtils;

    /**
     * 根据用户名查询认证用用户信息（未删除用户）。
     *
     * @param username 登录用户名
     * @return 认证 DTO，用户不存在时返回 {@code null}
     */
    @Override
    public SysUserAuthDto getAuthByUsername(String username) {
        SysUser user = findActiveUserByUsername(username);
        return user == null ? null : toAuthDto(user);
    }

    /**
     * 校验用户名与明文密码，通过后返回认证信息。
     *
     * @param username    登录用户名
     * @param rawPassword 明文密码
     * @return 认证 DTO，用户不存在或密码错误时返回 {@code null}
     */
    @Override
    public SysUserAuthDto verifyPassword(String username, String rawPassword) {
        SysUser user = findActiveUserByUsername(username);
        if (user == null || !StringUtils.hasText(rawPassword)) {
            return null;
        }
        if (!securityUtils.matchesPassword(rawPassword, user.getPassword())) {
            return null;
        }
        return toAuthDto(user);
    }

    /**
     * 按用户名查询未逻辑删除的用户。
     *
     * @param username 登录用户名
     * @return 用户实体，不存在时返回 {@code null}
     */
    private SysUser findActiveUserByUsername(String username) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, username)
                .eq(SysUser::getDelFlag, AppConstants.DelFlag.NORMAL));
    }

    /**
     * 将 {@link SysUser} 转换为认证服务所需的 {@link SysUserAuthDto}。
     *
     * @param user 用户实体
     * @return 含角色标识的认证 DTO
     */
    private SysUserAuthDto toAuthDto(SysUser user) {
        return SysUserAuthDto.builder()
                .userId(user.getUserId())
                .username(user.getUserName())
                .nickName(user.getNickName())
                .status(user.getStatus())
                .roles(sysUserMapper.selectRoleKeysByUserId(user.getUserId()))
                .avatar(user.getAvatar())
                .build();
    }

    /**
     * 获取用户可访问的菜单列表（含管理员与全部数据权限兜底）。
     *
     * @param userId 用户 ID
     * @return 菜单列表
     */
    @Override
    public List<SysMenu> getUserMenus(Long userId) {
        List<SysRole> roles = getRolesByUserId(userId);
        boolean isAdmin = roles != null && roles.stream()
                .anyMatch(role -> AppConstants.ADMIN_ROLE_KEY.equals(role.getRoleKey()));
        boolean hasAllDataScope = roles != null && roles.stream()
                .anyMatch(role -> AppConstants.DataScope.ALL.equals(role.getDataScope()));
        if (isAdmin || hasAllDataScope) {
            return sysUserMapper.selectAllActiveMenus();
        }
        return sysUserMapper.selectMenusByUserId(userId);
    }

    /**
     * 分页查询用户列表，按当前用户数据权限过滤。
     * <p>{@code @Transactional(readOnly = true)} 只读事务。</p>
     *
     * @param userReqBo 查询条件与分页参数
     * @return {@link UserVO} 分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserVO> pageList(UserReqBo userReqBo) {
        Page<SysUser> page = new Page<>(userReqBo.getPageNum(), userReqBo.getPageSize());
        Map<String, Object> param = buildDataScopeParam();
        param.put("userReqBo", userReqBo);
        IPage<SysUser> pageList = sysUserMapper.selectPageList(page, param);
        List<UserVO> voList = userVOAssembler.convertToVOList(pageList.getRecords());
        return toPageResponse(pageList, pageList.getCurrent(), pageList.getSize(), voList);
    }

    /**
     * 根据用户名查询用户（含已删除）。
     * <p>{@code @Transactional(readOnly = true)} 只读事务。</p>
     *
     * @param username 登录用户名
     * @return 用户实体，不存在时返回 {@code null}
     */
    @Override
    @Transactional(readOnly = true)
    public SysUser getUserByUsername(String username) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, username));
    }

    /**
     * 查询用户关联的角色列表。
     * <p>{@code @Transactional(readOnly = true)} 只读事务。</p>
     *
     * @param userId 用户 ID
     * @return 角色列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<SysRole> getRolesByUserId(Long userId) {
        return sysUserMapper.getRolesByUserId(userId);
    }

    /**
     * 查询用户及其角色信息（联表）。
     * <p>{@code @Transactional(readOnly = true)} 只读事务。</p>
     *
     * @param userId 用户 ID
     * @return 含角色集合的用户实体
     */
    @Override
    @Transactional(readOnly = true)
    public SysUser getUserWithRoles(Long userId) {
        return sysUserMapper.selectUserWithRoles(userId);
    }

    /**
     * 查询用户已授权的菜单列表。
     * <p>{@code @Transactional(readOnly = true)} 只读事务。</p>
     *
     * @param userId 用户 ID
     * @return 菜单列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<SysMenu> getMenuByUserId(Long userId) {
        return sysUserMapper.getMenuByUserId(userId);
    }

    /**
     * 新增用户，可选分配角色与岗位；未传密码时使用默认密码。
     * <p>{@code @Transactional} 异常时回滚。</p>
     *
     * @param userDTO 用户信息（可含 roleIds、postIds）
     * @return 是否保存成功
     * @throws BizException 用户名已存在时由 {@link AssertUtils} 抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUser(UserDTO userDTO) {
        AssertUtils.isNull(getUserByUsername(userDTO.getUserName()), ErrorCode.USER_USERNAME_EXISTS);
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(userDTO, sysUser);
        sysUser.setUserType("00");
        sysUser.setStatus(StringUtils.hasText(userDTO.getStatus()) ? userDTO.getStatus() : AppConstants.Status.NORMAL);
        sysUser.setDelFlag(AppConstants.DelFlag.NORMAL);
        sysUser.setPassword(StringUtils.hasText(userDTO.getPassword())
                ? securityUtils.encryptPassword(userDTO.getPassword())
                : securityUtils.encryptPassword("Star123456"));
        sysUser.setCreateBy(SecurityContextUtils.getUsername());
        sysUser.setCreateTime(LocalDateTime.now());

        boolean success = this.save(sysUser);
        if (success && userDTO.getRoleIds() != null && !userDTO.getRoleIds().isEmpty()) {
            insertUserRoles(sysUser.getUserId(), userDTO.getRoleIds());
        }
        if (success && userDTO.getPostIds() != null && !userDTO.getPostIds().isEmpty()) {
            insertUserPosts(sysUser.getUserId(), userDTO.getPostIds());
        }
        return success;
    }

    /**
     * 根据用户 ID 查询用户视图对象。
     * <p>{@code @Transactional(readOnly = true)} 只读事务。</p>
     *
     * @param userId 用户 ID
     * @return {@link UserVO}，用户不存在时返回 {@code null}
     */
    @Override
    @Transactional(readOnly = true)
    public UserVO selectByUserId(Long userId) {
        SysUser user = this.getById(userId);
        return user == null ? null : userVOAssembler.convertToVO(user);
    }

    /**
     * 更新用户信息；无完整更新权限时仅允许修改本人基本资料。
     * <p>{@code @Transactional} 异常时回滚；角色变更时清除用户权限缓存。</p>
     *
     * @param userDTO 用户信息（含 userId，可含 roleIds、postIds）
     * @return 是否更新成功
     * @throws BizException 用户不存在、用户名冲突或无权修改时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(UserDTO userDTO) {
        SysUser user = this.getById(userDTO.getUserId());
        if (user == null || AppConstants.DelFlag.DELETE.equals(user.getDelFlag())) {
            throw new BizException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        if (!canFullUpdateUser(userDTO.getUserId())) {
            return updateSelfProfile(user, userDTO);
        }

        SysUser existUser = getUserByUsername(userDTO.getUserName());
        if (existUser != null && !existUser.getUserId().equals(userDTO.getUserId())) {
            throw new BizException(ErrorCode.USER_USERNAME_USED, "用户名已被使用");
        }

        BeanUtils.copyProperties(userDTO, user, "password", "userId");
        user.setUpdateBy(SecurityContextUtils.getUsername());
        user.setUpdateTime(LocalDateTime.now());
        boolean success = this.updateById(user);

        if (success) {
            if (userDTO.getRoleIds() != null) {
                userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userDTO.getUserId()));
                if (!userDTO.getRoleIds().isEmpty()) {
                    insertUserRoles(userDTO.getUserId(), userDTO.getRoleIds());
                }
                userPermissionCacheService.clearUserPermissionCacheByUserId(userDTO.getUserId());
            }
            if (userDTO.getPostIds() != null) {
                userPostMapper.delete(new LambdaQueryWrapper<UserPost>().eq(UserPost::getUserId, userDTO.getUserId()));
                if (!userDTO.getPostIds().isEmpty()) {
                    insertUserPosts(userDTO.getUserId(), userDTO.getPostIds());
                }
            }
        }
        return success;
    }

    /**
     * 判断当前用户是否可对目标用户执行完整更新（超管或具备更新权限）。
     *
     * @param targetUserId 目标用户 ID
     * @return {@code true} 表示可完整更新
     */
    private boolean canFullUpdateUser(Long targetUserId) {
        if (isCurrentUserSuperAdmin()) {
            return true;
        }
        return SecurityContextUtils.hasAuthority("system:user:update");
    }

    /**
     * 当前用户修改本人基本资料（昵称、邮箱、手机、性别、头像）。
     *
     * @param user    待更新的用户实体
     * @param userDTO 含基本资料的 DTO
     * @return 是否更新成功
     * @throws BizException 非本人修改时抛出
     */
    private boolean updateSelfProfile(SysUser user, UserDTO userDTO) {
        Long currentUserId = SecurityContextUtils.getUserId();
        if (currentUserId == null || !currentUserId.equals(userDTO.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权修改该用户信息");
        }
        user.setNickName(userDTO.getNickName());
        user.setEmail(userDTO.getEmail());
        user.setPhonenumber(userDTO.getPhonenumber());
        user.setSex(userDTO.getSex());
        user.setAvatar(userDTO.getAvatar());
        user.setUpdateBy(SecurityContextUtils.getUsername());
        user.setUpdateTime(LocalDateTime.now());
        return this.updateById(user);
    }

    /**
     * 修改用户启用/停用状态。
     * <p>{@code @Transactional} 异常时回滚。</p>
     *
     * @param userId 用户 ID
     * @param status 目标状态
     * @return 是否更新成功
     * @throws BizException 用户不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeUserStatus(Long userId, String status) {
        SysUser user = this.getById(userId);
        if (user == null || AppConstants.DelFlag.DELETE.equals(user.getDelFlag())) {
            throw new BizException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }
        user.setStatus(status);
        user.setUpdateBy(SecurityContextUtils.getUsername());
        user.setUpdateTime(LocalDateTime.now());
        return this.updateById(user);
    }

    /**
     * 管理员重置用户密码。
     *
     * @param userId   用户 ID
     * @param password 新明文密码
     * @return 是否更新成功
     * @throws BizException 用户不存在时抛出
     */
    @Override
    public boolean resetUserPassword(Long userId, String password) {
        SysUser user = this.getById(userId);
        if (user == null || AppConstants.DelFlag.DELETE.equals(user.getDelFlag())) {
            throw new BizException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }
        user.setPassword(securityUtils.encryptPassword(password));
        user.setPwdUpdateDate(LocalDateTime.now());
        user.setUpdateBy(SecurityContextUtils.getUsername());
        user.setUpdateTime(LocalDateTime.now());
        boolean success = this.updateById(user);
        if (success) {
            userPermissionCacheService.clearUserPermissionCacheByUserId(userId);
        }
        return success;
    }

    /**
     * 用户修改自己的登录密码。
     * <p>{@code @Transactional} 异常时回滚，成功后清除权限缓存。</p>
     *
     * @param userId      用户 ID
     * @param oldPassword 旧明文密码
     * @param newPassword 新明文密码
     * @return 是否更新成功
     * @throws BizException 用户不存在、旧密码错误或新旧密码相同时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserPassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = this.getById(userId);
        if (user == null || AppConstants.DelFlag.DELETE.equals(user.getDelFlag())) {
            throw new BizException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }
        if (!securityUtils.matchesPassword(oldPassword, user.getPassword())) {
            throw new BizException(ErrorCode.USER_PASSWORD_ERROR, "旧密码不正确");
        }
        if (securityUtils.matchesPassword(newPassword, user.getPassword())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "新密码不能与旧密码相同");
        }
        user.setPassword(securityUtils.encryptPassword(newPassword));
        user.setPwdUpdateDate(LocalDateTime.now());
        user.setUpdateBy(SecurityContextUtils.getUsername());
        user.setUpdateTime(LocalDateTime.now());
        boolean success = this.updateById(user);
        if (success) {
            userPermissionCacheService.clearUserPermissionCacheByUserId(userId);
        }
        return success;
    }

    /**
     * 批量逻辑删除用户。
     * <p>{@code @Transactional} 异常时回滚。</p>
     *
     * @param userIds 待删除的用户 ID 列表
     * @return 有有效删除时返回 {@code true}，入参为空返回 {@code false}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUserByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return false;
        }
        return this.update(new LambdaUpdateWrapper<SysUser>()
                .in(SysUser::getUserId, userIds)
                .eq(SysUser::getDelFlag, AppConstants.DelFlag.NORMAL)
                .set(SysUser::getDelFlag, AppConstants.DelFlag.DELETE)
                .set(SysUser::getUpdateBy, SecurityContextUtils.getUsername())
                .set(SysUser::getUpdateTime, LocalDateTime.now()));
    }

    /**
     * 分页查询已分配指定角色的用户列表（含数据权限过滤）。
     *
     * @param assignUserReqBo 角色 ID 与分页参数
     * @return 用户分页结果
     */
    @Override
    public PageResponse<SysUser> getUserListByRoleId(AssignUserReqBo assignUserReqBo) {
        return queryPageWithDataScope(assignUserReqBo, sysUserMapper::getUserListByRoleId);
    }

    /**
     * 分页查询未分配指定角色的用户列表（含数据权限过滤）。
     *
     * @param assignUserReqBo 角色 ID 与分页参数
     * @return 用户分页结果
     */
    @Override
    public PageResponse<SysUser> unallocatedList(AssignUserReqBo assignUserReqBo) {
        return queryPageWithDataScope(assignUserReqBo, sysUserMapper::unallocatedList);
    }

    /**
     * 按查询条件导出用户 Excel 行数据（最多 5000 条）。
     *
     * @param userReqBo 查询条件
     * @return Excel 行列表
     */
    @Override
    public List<SysUserExcel> listForExport(UserReqBo userReqBo) {
        userReqBo.setPageNum(1);
        userReqBo.setPageSize(5000);
        PageResponse<UserVO> pageResponse = this.pageList(userReqBo);
        List<UserVO> userList = pageResponse.getRows();
        if (userList == null) {
            return List.of();
        }
        List<SysUserExcel> exportList = new ArrayList<>(userList.size());
        for (UserVO user : userList) {
            SysUserExcel row = new SysUserExcel();
            row.setUserName(user.getUserName());
            row.setNickName(user.getNickName());
            row.setEmail(user.getEmail());
            row.setPhonenumber(user.getPhonenumber());
            row.setSex(convertSexCodeToText(user.getSex()));
            row.setStatus(AppConstants.Status.NORMAL.equals(user.getStatus()) ? "正常" : "停用");
            row.setDeptId(user.getDeptId());
            row.setDeptName(user.getDeptName());
            row.setRemark(user.getRemark());
            exportList.add(row);
        }
        return exportList;
    }

    /**
     * 从 Excel 行批量导入用户，支持逐行新增或按用户名更新。
     *
     * @param rows           Excel 行数据
     * @param updateSupport  {@code true} 时存在同名用户则更新，否则仅新增
     * @return 导入结果（成功/失败计数及错误信息）
     * @throws BizException 导入数据为空时由 {@link AssertUtils} 抛出
     */
    @Override
    public ExcelImportResult importFromExcel(List<SysUserExcel> rows, boolean updateSupport) {
        AssertUtils.notEmpty(rows, ErrorCode.USER_IMPORT_EMPTY);
        ExcelImportResult result = new ExcelImportResult();
        int rowIndex = 1;
        for (SysUserExcel row : rows) {
            try {
                UserDTO userDTO = buildUserDTOFromExcel(row, rowIndex);
                saveOrUpdateFromImport(userDTO, updateSupport, rowIndex, result);
            } catch (BizException e) {
                result.setFailCount(result.getFailCount() + 1);
                result.addError("第 " + rowIndex + " 行：" + e.getMessage());
            } catch (Exception e) {
                result.setFailCount(result.getFailCount() + 1);
                result.addError("第 " + rowIndex + " 行导入失败：" + e.getMessage());
            } finally {
                rowIndex++;
            }
        }
        return result;
    }

    /**
     * 判断当前登录用户是否为超级管理员。
     *
     * @return {@code true} 表示是超级管理员（用户 ID 为 1 或拥有 admin 角色）
     */
    @Override
    public boolean isCurrentUserSuperAdmin() {
        Long currentUserId = SecurityContextUtils.getUserId();
        if (currentUserId == null) {
            return false;
        }
        if (AppConstants.ADMIN_USER_ID.equals(currentUserId)) {
            return true;
        }
        List<SysRole> roles = getRolesByUserId(currentUserId);
        return roles != null && roles.stream().anyMatch(role -> AppConstants.ADMIN_ROLE_KEY.equals(role.getRoleKey()));
    }

    /**
     * 判断当前用户是否可修改目标用户信息。
     *
     * @param targetUserId 目标用户 ID
     * @return {@code true} 表示超管或修改本人
     */
    @Override
    public boolean canUpdateUser(Long targetUserId) {
        Long currentUserId = SecurityContextUtils.getUserId();
        if (currentUserId == null) {
            return false;
        }
        return isCurrentUserSuperAdmin() || currentUserId.equals(targetUserId);
    }

    /**
     * 校验是否允许删除指定用户（不可删除当前登录用户）。
     *
     * @param userIds 待删除的用户 ID 列表
     * @return 不允许删除时返回错误提示，允许时返回 {@code null}
     */
    @Override
    public String canDeleteUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return "删除ID不能为空";
        }
        Long currentUserId = SecurityContextUtils.getUserId();
        for (Long userId : userIds) {
            if (userId.equals(currentUserId)) {
                return "不能删除当前登录用户";
            }
        }
        return null;
    }

    /**
     * 校验是否允许重置目标用户密码（不可重置当前登录用户）。
     *
     * @param targetUserId 目标用户 ID
     * @return 不允许时返回错误提示，允许时返回 {@code null}
     */
    @Override
    public String canResetPassword(Long targetUserId) {
        Long currentUserId = SecurityContextUtils.getUserId();
        if (currentUserId != null && currentUserId.equals(targetUserId)) {
            return "不能重置当前登录用户密码";
        }
        return null;
    }

    /**
     * 按月份统计指定时间范围内的新增用户数。
     *
     * @param start 起始时间（含）
     * @param end   结束时间（含）
     * @return 每月统计行，含 yearMonth 与 count 字段
     */
    @Override
    public List<Map<String, Object>> countByMonthRange(LocalDateTime start, LocalDateTime end) {
        return sysUserMapper.countByMonthRange(start, end);
    }

    /**
     * 构建含当前用户数据权限的 Mapper 查询参数。
     *
     * @return 含 dataScope、deptIds、userDeptId、userId 的参数 Map
     */
    private Map<String, Object> buildDataScopeParam() {
        DataScope dataScope = dataScopeService.getCurrentUserDataScope();
        Map<String, Object> param = new HashMap<>();
        param.put("dataScope", dataScope);
        param.put("deptIds", dataScope.getDeptIds());
        param.put("userDeptId", dataScope.getUserDeptId());
        param.put("userId", dataScope.getUserId());
        return param;
    }

    /**
     * 带数据权限的分页查询通用入口。
     *
     * @param bo    角色分配查询条件
     * @param query Mapper 分页查询函数
     * @return 用户分页结果
     */
    private PageResponse<SysUser> queryPageWithDataScope(AssignUserReqBo bo,
                                                         BiFunction<Page<SysUser>, Map<String, Object>, IPage<SysUser>> query) {
        Map<String, Object> param = buildDataScopeParam();
        param.put("assignUserReqBo", bo);
        Page<SysUser> page = new Page<>(bo.getPageNum(), bo.getPageSize());
        IPage<SysUser> result = query.apply(page, param);
        return toPageResponse(result, bo.getPageNum().longValue(), bo.getPageSize().longValue(), result.getRecords());
    }

    /**
     * 将 MyBatis-Plus 分页结果封装为 {@link PageResponse}。
     *
     * @param ipage    分页查询结果
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @param rows     当前页数据行
     * @param <T>      行数据类型
     * @return 统一分页响应
     */
    private <T> PageResponse<T> toPageResponse(IPage<?> ipage, long pageNum, long pageSize, List<T> rows) {
        PageResponse<T> resp = new PageResponse<>();
        resp.setTotal(ipage.getTotal());
        resp.setRows(rows);
        resp.setPageNum(pageNum);
        resp.setPageSize(pageSize);
        resp.setPageCount(ipage.getPages());
        return resp;
    }

    /**
     * 将性别编码转换为导出用中文文本。
     *
     * @param sexCode 性别编码（{@code 0} 男，{@code 1} 女）
     * @return 中文描述，未知时返回「未知」
     */
    private String convertSexCodeToText(String sexCode) {
        if (!StringUtils.hasText(sexCode)) {
            return "未知";
        }
        return switch (sexCode) {
            case "0" -> "男";
            case "1" -> "女";
            default -> "未知";
        };
    }

    /**
     * 将 Excel 行解析为 {@link UserDTO}，并校验必填字段。
     *
     * @param row      Excel 行
     * @param rowIndex 行号（用于错误提示）
     * @return 用户 DTO
     * @throws BizException 行数据为空或必填项缺失时抛出
     */
    private UserDTO buildUserDTOFromExcel(SysUserExcel row, int rowIndex) {
        if (row == null) {
            throw new BizException(ErrorCode.USER_IMPORT_ROW_EMPTY, "第 " + rowIndex + " 行数据为空");
        }
        if (!StringUtils.hasText(row.getUserName())) {
            throw new BizException(ErrorCode.USER_IMPORT_USERNAME_EMPTY, "第 " + rowIndex + " 行用户账号不能为空");
        }
        if (!StringUtils.hasText(row.getNickName())) {
            throw new BizException(ErrorCode.USER_IMPORT_NICKNAME_EMPTY, "第 " + rowIndex + " 行用户昵称不能为空");
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setUserName(row.getUserName().trim());
        userDTO.setNickName(row.getNickName().trim());
        if (StringUtils.hasText(row.getEmail())) {
            userDTO.setEmail(row.getEmail().trim());
        }
        if (StringUtils.hasText(row.getPhonenumber())) {
            userDTO.setPhonenumber(row.getPhonenumber().trim());
        }
        String sexCode = "2";
        if (StringUtils.hasText(row.getSex())) {
            String sexText = row.getSex().trim();
            if ("男".equals(sexText)) {
                sexCode = "0";
            } else if ("女".equals(sexText)) {
                sexCode = "1";
            }
        }
        userDTO.setSex(sexCode);
        String statusCode = AppConstants.Status.NORMAL;
        if (StringUtils.hasText(row.getStatus())) {
            String statusText = row.getStatus().trim();
            if ("停用".equals(statusText) || "禁用".equals(statusText)) {
                statusCode = AppConstants.Status.DISABLE;
            }
        }
        userDTO.setStatus(statusCode);
        userDTO.setDeptId(row.getDeptId());
        if (StringUtils.hasText(row.getRemark())) {
            userDTO.setRemark(row.getRemark().trim());
        }
        return userDTO;
    }

    /**
     * 批量插入用户与角色的关联关系。
     *
     * @param userId  用户 ID
     * @param roleIds 角色 ID 列表
     */
    private void insertUserRoles(Long userId, List<Long> roleIds) {
        List<UserRole> userRoles = new ArrayList<>(roleIds.size());
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoles.add(userRole);
        }
        userRoleMapper.insertBatchUserRoles(userRoles);
    }

    /**
     * 批量插入用户与岗位的关联关系。
     *
     * @param userId  用户 ID
     * @param postIds 岗位 ID 列表
     */
    private void insertUserPosts(Long userId, List<Long> postIds) {
        List<UserPost> userPosts = new ArrayList<>(postIds.size());
        for (Long postId : postIds) {
            UserPost userPost = new UserPost();
            userPost.setUserId(userId);
            userPost.setPostId(postId);
            userPosts.add(userPost);
        }
        userPostMapper.insertBatchUserPosts(userPosts);
    }

    /**
     * 导入单行用户：按 updateSupport 决定新增或更新，并累计导入结果。
     *
     * @param userDTO        解析后的用户 DTO
     * @param updateSupport  是否支持按用户名更新
     * @param rowIndex       行号（用于错误提示）
     * @param result         导入结果累加器
     */
    private void saveOrUpdateFromImport(UserDTO userDTO, boolean updateSupport, int rowIndex, ExcelImportResult result) {
        boolean success;
        if (updateSupport) {
            SysUser existing = getUserByUsername(userDTO.getUserName());
            if (existing != null) {
                userDTO.setUserId(existing.getUserId());
                success = Boolean.TRUE.equals(transactionTemplate.execute(status -> updateUser(userDTO)));
            } else {
                success = Boolean.TRUE.equals(transactionTemplate.execute(status -> addUser(userDTO)));
            }
        } else {
            success = Boolean.TRUE.equals(transactionTemplate.execute(status -> addUser(userDTO)));
        }
        if (success) {
            result.setSuccessCount(result.getSuccessCount() + 1);
        } else {
            result.setFailCount(result.getFailCount() + 1);
            result.addError("第 " + rowIndex + " 行" + (updateSupport ? "更新" : "新增") + "失败");
        }
    }

    /**
     * 用户自助注册，分配默认角色。
     * <p>{@code @Transactional} 异常时回滚。</p>
     *
     * @param request 注册请求（用户名、密码）
     * @return 注册成功后的用户摘要
     * @throws BizException 用户名已存在或保存失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterUserResponse registerUser(RegisterUserRequest request) {
        String username = request.getUsername().trim();
        AssertUtils.isNull(getUserByUsername(username), ErrorCode.USER_USERNAME_EXISTS);

        SysUser user = new SysUser();
        user.setUserName(username);
        user.setNickName(username);
        user.setUserType("00");
        user.setStatus(AppConstants.Status.NORMAL);
        user.setDelFlag(AppConstants.DelFlag.NORMAL);
        user.setPassword(SecurityUtils.encryptPassword(request.getPassword()));
        user.setCreateBy(username);
        user.setCreateTime(LocalDateTime.now());
        if (!this.save(user)) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "注册失败，请稍后重试");
        }

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getUserId());
        userRole.setRoleId(AppConstants.DEFAULT_REGISTER_ROLE_ID);
        userRoleMapper.insertBatchUserRoles(List.of(userRole));

        return RegisterUserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUserName())
                .nickName(user.getNickName())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPasswordByForgot(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return false;
        }
        SysUser user = getUserByUsername(username.trim());
        if (user == null || AppConstants.DelFlag.DELETE.equals(user.getDelFlag())) {
            return false;
        }
        if (!AppConstants.Status.NORMAL.equals(user.getStatus())) {
            return false;
        }
        user.setPassword(SecurityUtils.encryptPassword(password));
        user.setPwdUpdateDate(LocalDateTime.now());
        user.setUpdateBy(username.trim());
        user.setUpdateTime(LocalDateTime.now());
        boolean success = this.updateById(user);
        if (success) {
            userPermissionCacheService.clearUserPermissionCacheByUserId(user.getUserId());
        }
        return success;
    }
}
