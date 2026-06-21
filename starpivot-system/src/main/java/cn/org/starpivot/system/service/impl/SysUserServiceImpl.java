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

    @Override
    public SysUserAuthDto getAuthByUsername(String username) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, username)
                .eq(SysUser::getDelFlag, AppConstants.DelFlag.NORMAL));
        if (user == null) {
            return null;
        }
        return SysUserAuthDto.builder()
                .userId(user.getUserId())
                .username(user.getUserName())
                .password(user.getPassword())
                .nickName(user.getNickName())
                .status(user.getStatus())
                .roles(sysUserMapper.selectRoleKeysByUserId(user.getUserId()))
                .avatar(user.getAvatar())
                .build();
    }

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

    @Override
    @Transactional(readOnly = true)
    public SysUser getUserByUsername(String username) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, username));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SysRole> getRolesByUserId(Long userId) {
        return sysUserMapper.getRolesByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public SysUser getUserWithRoles(Long userId) {
        return sysUserMapper.selectUserWithRoles(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SysMenu> getMenuByUserId(Long userId) {
        return sysUserMapper.getMenuByUserId(userId);
    }

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

    @Override
    @Transactional(readOnly = true)
    public UserVO selectByUserId(Long userId) {
        SysUser user = this.getById(userId);
        return user == null ? null : userVOAssembler.convertToVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(UserDTO userDTO) {
        SysUser user = this.getById(userDTO.getUserId());
        if (user == null || AppConstants.DelFlag.DELETE.equals(user.getDelFlag())) {
            throw new BizException(ErrorCode.USER_NOT_FOUND, "用户不存在");
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
                userPermissionCacheService.clearUserPermissionCache(user.getUserName());
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
            userPermissionCacheService.clearUserPermissionCache(user.getUserName());
        }
        return success;
    }

    @Override
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
            userPermissionCacheService.clearUserPermissionCache(user.getUserName());
        }
        return success;
    }

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

    @Override
    public PageResponse<SysUser> getUserListByRoleId(AssignUserReqBo assignUserReqBo) {
        return queryPageWithDataScope(assignUserReqBo, sysUserMapper::getUserListByRoleId);
    }

    @Override
    public PageResponse<SysUser> unallocatedList(AssignUserReqBo assignUserReqBo) {
        return queryPageWithDataScope(assignUserReqBo, sysUserMapper::unallocatedList);
    }

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

    @Override
    public boolean canUpdateUser(Long targetUserId) {
        Long currentUserId = SecurityContextUtils.getUserId();
        if (currentUserId == null) {
            return false;
        }
        return isCurrentUserSuperAdmin() || currentUserId.equals(targetUserId);
    }

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

    @Override
    public String canResetPassword(Long targetUserId) {
        Long currentUserId = SecurityContextUtils.getUserId();
        if (currentUserId != null && currentUserId.equals(targetUserId)) {
            return "不能重置当前登录用户密码";
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> countByMonthRange(LocalDateTime start, LocalDateTime end) {
        return sysUserMapper.countByMonthRange(start, end);
    }

    private Map<String, Object> buildDataScopeParam() {
        DataScope dataScope = dataScopeService.getCurrentUserDataScope();
        Map<String, Object> param = new HashMap<>();
        param.put("dataScope", dataScope);
        param.put("deptIds", dataScope.getDeptIds());
        param.put("userDeptId", dataScope.getUserDeptId());
        param.put("userId", dataScope.getUserId());
        return param;
    }

    private PageResponse<SysUser> queryPageWithDataScope(AssignUserReqBo bo,
                                                         BiFunction<Page<SysUser>, Map<String, Object>, IPage<SysUser>> query) {
        Map<String, Object> param = buildDataScopeParam();
        param.put("assignUserReqBo", bo);
        Page<SysUser> page = new Page<>(bo.getPageNum(), bo.getPageSize());
        IPage<SysUser> result = query.apply(page, param);
        return toPageResponse(result, bo.getPageNum().longValue(), bo.getPageSize().longValue(), result.getRecords());
    }

    private <T> PageResponse<T> toPageResponse(IPage<?> ipage, long pageNum, long pageSize, List<T> rows) {
        PageResponse<T> resp = new PageResponse<>();
        resp.setTotal(ipage.getTotal());
        resp.setRows(rows);
        resp.setPageNum(pageNum);
        resp.setPageSize(pageSize);
        resp.setPageCount(ipage.getPages());
        return resp;
    }

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
}
