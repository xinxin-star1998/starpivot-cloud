package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.common.cache.CacheConstants;
import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.entity.PageResponse;
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
import cn.org.starpivot.system.mapper.*;
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

/**
 * 角色管理服务实现类。
 * <p>
 * 实现 {@link SysRoleService}，含角色 CRUD、菜单/部门权限分配及用户授权；
 * 变更时清除相关缓存。
 * </p>
 */
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

    /**
     * 分页查询角色列表。
     * <p>{@code @Transactional(readOnly = true)} 只读事务。</p>
     *
     * @param roleQueryDTO 查询条件与分页参数
     * @return 角色分页结果
     */
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

    /**
     * 根据角色 ID 查询角色详情。
     * <p>{@code @Transactional(readOnly = true)} 只读事务。</p>
     *
     * @param roleId 角色 ID
     * @return 角色实体，不存在时返回 {@code null}
     */
    @Override
    @Transactional(readOnly = true)
    public SysRole selectRoleById(Long roleId) {
        return sysRoleMapper.selectById(roleId);
    }

    /**
     * 查询全部正常状态的角色列表（下拉选用）。
     * <p>{@code @Transactional(readOnly = true)} 只读事务；{@code @Cacheable} 缓存至 {@link CacheConstants#ROLE_LIST}。</p>
     *
     * @return 按排序号排列的角色列表
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConstants.ROLE_LIST, key = "'all'")
    public List<SysRole> selectAllRoles() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getDelFlag, AppConstants.DelFlag.NORMAL)
                .eq(SysRole::getStatus, AppConstants.Status.NORMAL)
                .orderByAsc(SysRole::getRoleSort);
        return this.list(wrapper);
    }

    /**
     * 新增角色，可选同时分配菜单权限。
     * <p>{@code @CacheEvict} 清空角色列表缓存；{@code @Transactional} 异常时回滚。</p>
     *
     * @param roleDTO 角色信息（可含 menuIds）
     * @return 是否保存成功
     * @throws BizException 角色权限字符串已存在时抛出
     */
    @Override
    @CacheEvict(cacheNames = CacheConstants.ROLE_LIST, key = "'all'")
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

    /**
     * 更新角色信息，可选重新分配菜单权限。
     * <p>{@code @CacheEvict} 清空角色列表缓存；{@code @Transactional} 异常时回滚。</p>
     *
     * @param roleDTO 角色信息（含 roleId，可含 menuIds）
     * @return 是否更新成功
     * @throws BizException 角色不存在、权限字符串冲突或修改超级管理员时抛出
     */
    @Override
    @CacheEvict(cacheNames = CacheConstants.ROLE_LIST, key = "'all'")
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

    /**
     * 批量逻辑删除角色。
     * <p>{@code @CacheEvict} 清空角色列表缓存；{@code @Transactional} 异常时回滚。</p>
     *
     * @param roleIds 待删除的角色 ID 列表
     * @return 有有效删除时返回 {@code true}，入参为空或角色均不存在返回 {@code false}
     * @throws BizException 删除超级管理员或已被用户使用的角色时抛出
     */
    @Override
    @CacheEvict(cacheNames = CacheConstants.ROLE_LIST, key = "'all'")
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

    /**
     * 修改角色启用/停用状态。
     * <p>{@code @CacheEvict} 清空角色列表缓存。</p>
     *
     * @param roleId 角色 ID
     * @param status 目标状态（{@code 0} 正常，{@code 1} 停用）
     * @return 是否更新成功
     * @throws BizException 角色不存在或停用超级管理员时抛出
     */
    @Override
    @CacheEvict(cacheNames = CacheConstants.ROLE_LIST, key = "'all'")
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

    /**
     * 查询角色可访问的部门 ID 列表（数据权限）。
     * <p>{@code @Transactional(readOnly = true)} 只读事务。超级管理员返回全部部门。</p>
     *
     * @param roleId 角色 ID
     * @return 部门 ID 列表
     * @throws BizException 角色不存在、已删除或已禁用时抛出
     */
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

    /**
     * 查询角色已分配的菜单 ID 列表。
     * <p>{@code @Transactional(readOnly = true)} 只读事务。超级管理员返回全部菜单。</p>
     *
     * @param roleId 角色 ID
     * @return 菜单 ID 列表
     * @throws BizException 角色不存在、已删除或已禁用时抛出
     */
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

    /**
     * 为角色批量授权用户。
     * <p>{@code @Transactional} 异常时回滚。</p>
     *
     * @param userRoleDTO 角色 ID 与用户 ID 列表
     * @return 用户列表为空时返回 {@code true}，否则批量插入后返回 {@code true}
     */
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

    /**
     * 取消单个用户的角色授权。
     *
     * @param userRole 含 roleId 与 userId 的关联对象
     * @return 是否删除成功
     */
    @Override
    public boolean cancelUser(UserRole userRole) {
        return userRoleMapper.deleteByRoleIdAndUserId(userRole.getRoleId(), userRole.getUserId());
    }

    /**
     * 分配角色数据权限（数据范围 + 自定义部门）。
     * <p>{@code @Transactional} 异常时回滚，成功后清除全部用户权限缓存。</p>
     *
     * @param rolePermissionAssignDTO 角色 ID、数据范围及部门 ID 列表
     * @return 是否分配成功
     * @throws BizException 角色不存在时抛出
     */
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

    /**
     * 批量保存角色与菜单的关联关系。
     *
     * @param roleId  角色 ID
     * @param menuIds 菜单 ID 列表，为空时直接返回
     */
    private void insertRoleMenus(Long roleId, List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        roleMenuMapper.batchSave(roleId, menuIds);
    }
}
