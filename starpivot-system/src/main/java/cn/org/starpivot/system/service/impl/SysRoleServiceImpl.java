package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.common.domain.PageResponse;
import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.common.util.AssertUtils;
import cn.org.starpivot.system.domain.dto.RoleDTO;
import cn.org.starpivot.system.domain.dto.RolePermissionAssignDTO;
import cn.org.starpivot.system.domain.dto.RoleQueryDTO;
import cn.org.starpivot.system.domain.dto.UserRoleDTO;
import cn.org.starpivot.system.domain.entity.RoleMenu;
import cn.org.starpivot.system.domain.entity.SysRole;
import cn.org.starpivot.system.domain.entity.UserRole;
import cn.org.starpivot.system.mapper.RoleDeptMapper;
import cn.org.starpivot.system.mapper.RoleMenuMapper;
import cn.org.starpivot.system.mapper.SysDeptMapper;
import cn.org.starpivot.system.mapper.SysMenuMapper;
import cn.org.starpivot.system.mapper.SysRoleMapper;
import cn.org.starpivot.system.mapper.UserRoleMapper;
import cn.org.starpivot.system.service.SysRoleService;
import cn.org.starpivot.system.service.UserPermissionCacheService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service("sysRoleService")
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final RoleDeptMapper roleDeptMapper;
    private final UserRoleMapper userRoleMapper;
    private final SysDeptMapper sysDeptMapper;
    private final SysMenuMapper sysMenuMapper;
    private final UserPermissionCacheService userPermissionCacheService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SysRole> selectRoleList(RoleQueryDTO roleQueryDTO) {
        PageResponse<SysRole> pageResponse = new PageResponse<>();
        Page<SysRole> page = new Page<>(roleQueryDTO.getPageNum(), roleQueryDTO.getPageSize());
        IPage<SysRole> pageList = sysRoleMapper.selectPageList(page, roleQueryDTO);
        pageResponse.setTotal(pageList.getTotal());
        pageResponse.setRows(pageList.getRecords());
        pageResponse.setPageNum(Long.valueOf(roleQueryDTO.getPageNum()));
        pageResponse.setPageSize(Long.valueOf(roleQueryDTO.getPageSize()));
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public SysRole selectRoleById(Long roleId) {
        return sysRoleMapper.selectById(roleId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "roleList", key = "'all'")
    public List<SysRole> selectAllRoles() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getDelFlag, AppConstants.DelFlag.NORMAL)
                .eq(SysRole::getStatus, AppConstants.Status.NORMAL)
                .orderByAsc(SysRole::getRoleSort);
        return this.list(wrapper);
    }

    @Override
    @CacheEvict(cacheNames = "roleList", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public boolean insertRole(RoleDTO roleDTO) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleKey, roleDTO.getRoleKey())
                .eq(SysRole::getDelFlag, "0");
        if (this.count(wrapper) > 0) {
            throw new BizException(ErrorCode.ROLE_KEY_EXISTS, "角色权限字符串已存在");
        }

        SysRole role = new SysRole();
        BeanUtils.copyProperties(roleDTO, role);
        role.setStatus(StringUtils.hasText(roleDTO.getStatus()) ? roleDTO.getStatus() : "0");
        role.setDelFlag(AppConstants.DelFlag.NORMAL);
        role.setMenuCheckStrictly(roleDTO.getMenuCheckStrictly() != null ? roleDTO.getMenuCheckStrictly() : 1);
        role.setDeptCheckStrictly(roleDTO.getDeptCheckStrictly() != null ? roleDTO.getDeptCheckStrictly() : 1);

        String currentUser = SecurityContextUtils.getUsername();
        role.setCreateBy(currentUser);
        role.setCreateTime(LocalDateTime.now());

        boolean success = this.save(role);

        if (success && roleDTO.getMenuIds() != null && !roleDTO.getMenuIds().isEmpty()) {
            insertRoleMenus(role.getRoleId(), roleDTO.getMenuIds());
        }
        return success;
    }

    @Override
    @CacheEvict(cacheNames = "roleList", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(RoleDTO roleDTO) {
        SysRole role = this.getById(roleDTO.getRoleId());
        AssertUtils.notNull(role, ErrorCode.ROLE_NOT_FOUND);
        if ("2".equals(role.getDelFlag())) {
            throw new BizException(ErrorCode.ROLE_NOT_FOUND, "角色不存在");
        }

        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleKey, roleDTO.getRoleKey())
                .eq(SysRole::getDelFlag, "0")
                .ne(SysRole::getRoleId, roleDTO.getRoleId());
        if (this.count(wrapper) > 0) {
            throw new BizException(ErrorCode.ROLE_KEY_USED, "角色权限字符串已被使用");
        }

        if (AppConstants.ADMIN_ROLE_KEY.equals(role.getRoleKey())
                && !AppConstants.ADMIN_ROLE_KEY.equals(roleDTO.getRoleKey())) {
            throw new BizException(ErrorCode.ROLE_ADMIN_PROTECTED, "不允许修改超级管理员角色");
        }

        BeanUtils.copyProperties(roleDTO, role, "roleId");
        String currentUser = SecurityContextUtils.getUsername();
        role.setUpdateBy(currentUser);
        role.setUpdateTime(LocalDateTime.now());

        boolean success = this.updateById(role);

        if (success && roleDTO.getMenuIds() != null) {
            LambdaQueryWrapper<RoleMenu> menuWrapper = new LambdaQueryWrapper<>();
            menuWrapper.eq(RoleMenu::getRoleId, roleDTO.getRoleId());
            roleMenuMapper.delete(menuWrapper);

            if (!roleDTO.getMenuIds().isEmpty()) {
                insertRoleMenus(roleDTO.getRoleId(), roleDTO.getMenuIds());
            }
        }
        return success;
    }

    @Override
    @CacheEvict(cacheNames = "roleList", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRoleByIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return false;
        }

        List<SysRole> roles = this.listByIds(roleIds);
        if (roles.isEmpty()) {
            return false;
        }

        String currentUser = SecurityContextUtils.getUsername();
        LocalDateTime now = LocalDateTime.now();
        List<SysRole> toUpdate = new ArrayList<>();

        for (SysRole role : roles) {
            if ("2".equals(role.getDelFlag())) {
                continue;
            }
            if (AppConstants.ADMIN_ROLE_KEY.equals(role.getRoleKey())) {
                throw new BizException(ErrorCode.ROLE_ADMIN_PROTECTED, "不允许删除超级管理员角色");
            }

            LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserRole::getRoleId, role.getRoleId());
            long count = userRoleMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BizException(ErrorCode.ROLE_USED, "角色[" + role.getRoleName() + "]已被使用，不能删除");
            }

            role.setDelFlag(AppConstants.DelFlag.DELETE);
            role.setUpdateBy(currentUser);
            role.setUpdateTime(now);
            toUpdate.add(role);
        }

        if (!toUpdate.isEmpty()) {
            this.updateBatchById(toUpdate);
        }
        return true;
    }

    @Override
    @CacheEvict(cacheNames = "roleList", allEntries = true)
    public boolean changeRoleStatus(Long roleId, String status) {
        SysRole role = this.getById(roleId);
        AssertUtils.notNull(role, ErrorCode.ROLE_NOT_FOUND);
        if ("2".equals(role.getDelFlag())) {
            throw new BizException(ErrorCode.ROLE_NOT_FOUND, "角色不存在");
        }

        if (AppConstants.ADMIN_ROLE_KEY.equals(role.getRoleKey()) && "1".equals(status)) {
            throw new BizException(ErrorCode.ROLE_ADMIN_PROTECTED, "不允许停用超级管理员角色");
        }

        role.setStatus(status);
        String currentUser = SecurityContextUtils.getUsername();
        role.setUpdateBy(currentUser);
        role.setUpdateTime(LocalDateTime.now());

        return this.updateById(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> selectDeptIdsByRoleId(Long roleId) {
        SysRole sysRole = sysRoleMapper.selectById(roleId);
        AssertUtils.notNull(sysRole, ErrorCode.ROLE_NOT_FOUND);
        if ("2".equals(sysRole.getDelFlag())) {
            throw new BizException(ErrorCode.ROLE_DELETED, "角色已删除");
        }
        if ("1".equals(sysRole.getStatus())) {
            throw new BizException(ErrorCode.ROLE_DISABLED, "角色已禁用，请联系管理员");
        }
        if (AppConstants.ADMIN_ROLE_KEY.equals(sysRole.getRoleKey())) {
            return sysDeptMapper.selectAllDeptIds();
        }
        return roleDeptMapper.selectDeptIdsByRoleId(roleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        SysRole sysRole = sysRoleMapper.selectById(roleId);
        if (sysRole == null) {
            throw new BizException(ErrorCode.ROLE_NOT_FOUND, "角色不存在");
        }
        if ("2".equals(sysRole.getDelFlag())) {
            throw new BizException(ErrorCode.ROLE_DELETED, "角色已删除");
        }
        if ("1".equals(sysRole.getStatus())) {
            throw new BizException(ErrorCode.ROLE_DISABLED, "角色已禁用，请联系管理员");
        }
        if (AppConstants.ADMIN_ROLE_KEY.equals(sysRole.getRoleKey())) {
            return sysMenuMapper.selectMenuIds(null);
        }
        List<Long> menuIds = roleMenuMapper.selectMenuIdsByRoleId(roleId);
        return menuIds != null ? menuIds : Collections.emptyList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignUser(UserRoleDTO userRoleDTO) {
        if (userRoleDTO.getUserIds() == null || userRoleDTO.getUserIds().isEmpty()) {
            return true;
        }
        List<UserRole> userRoles = new ArrayList<>(userRoleDTO.getUserIds().size());
        for (Long userId : userRoleDTO.getUserIds()) {
            UserRole userRole = new UserRole();
            userRole.setRoleId(userRoleDTO.getRoleId());
            userRole.setUserId(userId);
            userRoles.add(userRole);
        }
        userRoleMapper.insertBatchUserRoles(userRoles);
        return true;
    }

    @Override
    public boolean cancelUser(UserRole userRole) {
        return userRoleMapper.deleteByRoleIdAndUserId(userRole.getRoleId(), userRole.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermission(RolePermissionAssignDTO rolePermissionAssignDTO) {
        Long roleId = rolePermissionAssignDTO.getRoleId();
        List<Long> deptIds = rolePermissionAssignDTO.getDeptIds() == null
                ? Collections.emptyList()
                : rolePermissionAssignDTO.getDeptIds();

        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null || AppConstants.DelFlag.DELETE.equals(role.getDelFlag())) {
            throw new BizException(ErrorCode.ROLE_NOT_FOUND, "角色不存在");
        }

        if (StringUtils.hasText(rolePermissionAssignDTO.getDataScope())) {
            role.setDataScope(rolePermissionAssignDTO.getDataScope());
            sysRoleMapper.updateById(role);
        }

        roleDeptMapper.deleteByRoleId(roleId);
        if (!deptIds.isEmpty()) {
            roleDeptMapper.batchSave(roleId, deptIds);
        }

        userPermissionCacheService.clearAllUserPermissionCache();
        return true;
    }

    private void insertRoleMenus(Long roleId, List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        roleMenuMapper.batchSave(roleId, menuIds);
    }
}
