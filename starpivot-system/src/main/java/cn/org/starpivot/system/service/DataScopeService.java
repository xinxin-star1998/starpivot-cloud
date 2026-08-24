package cn.org.starpivot.system.service;

import cn.org.starpivot.common.datascope.DataScopeProvider;
import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.entity.DataScope;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.system.domain.entity.SysDept;
import cn.org.starpivot.system.domain.entity.SysRole;
import cn.org.starpivot.system.domain.entity.SysUser;
import cn.org.starpivot.system.mapper.RoleDeptMapper;
import cn.org.starpivot.system.mapper.SysDeptMapper;
import cn.org.starpivot.system.mapper.SysRoleMapper;
import cn.org.starpivot.system.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 数据权限范围服务。
 * <p>
 * 作为唯一的 {@link DataScopeProvider} 实现：多角色按并集合并，
 * 供拦截器过滤查询，并提供写操作的目标可见性校验。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataScopeService implements DataScopeProvider {

    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final RoleDeptMapper roleDeptMapper;

    /**
     * 计算当前登录用户的数据权限范围。
     *
     * @return 数据范围上下文；未登录时无可见数据
     */
    public DataScope getCurrentUserDataScope() {
        return resolve(SecurityContextUtils.getUserId());
    }

    /**
     * 当前用户是否可访问目标用户（本人、全部权限、或目标部门在范围内）。
     *
     * @param targetUserId 目标用户 ID
     * @return 可见返回 {@code true}
     */
    public boolean isTargetUserAccessible(Long targetUserId) {
        if (targetUserId == null) {
            return false;
        }
        Long currentUserId = SecurityContextUtils.getUserId();
        if (currentUserId == null) {
            return false;
        }
        if (currentUserId.equals(targetUserId)) {
            return true;
        }
        DataScope scope = resolve(currentUserId);
        if (scope.isAll()) {
            return true;
        }
        SysUser target = userMapper.selectById(targetUserId);
        if (target == null || target.getDeptId() == null) {
            return false;
        }
        List<Long> deptIds = scope.getDeptIds();
        return deptIds != null && deptIds.contains(target.getDeptId());
    }

    /**
     * 当前用户是否可在指定部门下创建/归属数据。
     *
     * @param deptId 目标部门 ID
     * @return 全部权限或部门在范围内时返回 {@code true}
     */
    public boolean isDeptAccessible(Long deptId) {
        List<Long> visibleDeptIds = getVisibleDeptIds();
        if (visibleDeptIds == null) {
            return true;
        }
        return deptId != null && visibleDeptIds.contains(deptId);
    }

    /**
     * 当前用户可见的部门 ID。
     *
     * @return {@code null} 表示全部部门；空列表表示没有可见部门
     */
    public List<Long> getVisibleDeptIds() {
        DataScope scope = getCurrentUserDataScope();
        if (scope.isAll()) {
            return null;
        }
        Set<Long> ids = new LinkedHashSet<>();
        if (scope.getDeptIds() != null) {
            ids.addAll(scope.getDeptIds());
        }
        if (scope.isIncludeSelf() && scope.getUserDeptId() != null) {
            ids.add(scope.getUserDeptId());
        }
        return new ArrayList<>(ids);
    }

    /**
     * 当前用户可见的用户名（公告创建人、操作人、登录账号等按用户名归属的数据）。
     *
     * @return {@code null} 表示不限制；空列表表示没有可见数据
     */
    public List<String> getVisibleUserNames() {
        DataScope scope = getCurrentUserDataScope();
        if (scope.isAll()) {
            return null;
        }
        Set<String> names = new LinkedHashSet<>();
        String current = SecurityContextUtils.getUsername();
        if (scope.isIncludeSelf() && current != null && !current.isBlank()) {
            names.add(current);
        }
        List<Long> deptIds = scope.getDeptIds();
        if (deptIds != null && !deptIds.isEmpty()) {
            List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                    .select(SysUser::getUserName)
                    .in(SysUser::getDeptId, deptIds)
                    .eq(SysUser::getDelFlag, AppConstants.DelFlag.NORMAL));
            if (users != null) {
                for (SysUser user : users) {
                    if (user.getUserName() != null && !user.getUserName().isBlank()) {
                        names.add(user.getUserName());
                    }
                }
            }
        }
        return new ArrayList<>(names);
    }

    /**
     * 指定用户名是否在当前数据范围内。
     *
     * @param username 用户名
     * @return 全部权限或用户名在可见集合中时返回 {@code true}
     */
    public boolean isUserNameVisible(String username) {
        List<String> names = getVisibleUserNames();
        if (names == null) {
            return true;
        }
        return username != null && names.contains(username);
    }

    @Override
    public DataScope resolve(Long userId) {
        if (userId == null) {
            return DataScope.none(null);
        }

        SysUser user = userMapper.selectById(userId);
        Long userDeptId = user != null ? user.getDeptId() : null;

        if (AppConstants.ADMIN_USER_ID.equals(userId)) {
            return DataScope.all(userId, userDeptId);
        }

        List<SysRole> roleList = roleMapper.selectRoleListByUserId(userId);
        if (roleList == null || roleList.isEmpty()) {
            return DataScope.none(userId, userDeptId);
        }

        boolean includeSelf = false;
        Set<Long> deptIdSet = new LinkedHashSet<>();

        for (SysRole role : roleList) {
            if (!AppConstants.Status.NORMAL.equals(role.getStatus())) {
                continue;
            }
            if (AppConstants.ADMIN_ROLE_KEY.equals(role.getRoleKey())) {
                return DataScope.all(userId, userDeptId);
            }
            String scope = Optional.ofNullable(role.getDataScope())
                    .map(String::trim)
                    .orElse(AppConstants.DataScope.SELF);
            switch (scope) {
                case AppConstants.DataScope.ALL -> {
                    return DataScope.all(userId, userDeptId);
                }
                case AppConstants.DataScope.CUSTOM -> addCustomDeptIds(role.getRoleId(), deptIdSet);
                case AppConstants.DataScope.DEPT -> {
                    if (userDeptId != null) {
                        deptIdSet.add(userDeptId);
                    }
                }
                case AppConstants.DataScope.DEPT_AND_CHILD -> deptIdSet.addAll(getDeptAndChildIds(userDeptId));
                default -> includeSelf = true;
            }
        }

        return DataScope.restricted(userId, userDeptId, new ArrayList<>(deptIdSet), includeSelf);
    }

    private void addCustomDeptIds(Long roleId, Set<Long> deptIdSet) {
        List<Long> ids = roleDeptMapper.selectDeptIdsByRoleId(roleId);
        if (ids != null) {
            deptIdSet.addAll(ids);
        }
    }

    /**
     * 本部门及子孙部门。使用 {@code FIND_IN_SET(deptId, ancestors)}，避免 LIKE 误匹配。
     */
    private List<Long> getDeptAndChildIds(Long deptId) {
        if (deptId == null) {
            return List.of();
        }
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getStatus, AppConstants.Status.NORMAL)
                .and(w -> w.eq(SysDept::getDeptId, deptId)
                        .or()
                        .apply("FIND_IN_SET({0}, ancestors)", deptId));
        List<SysDept> depts = deptMapper.selectList(wrapper);
        List<Long> ids = new ArrayList<>(depts.size());
        for (SysDept dept : depts) {
            if (dept.getDeptId() != null) {
                ids.add(dept.getDeptId());
            }
        }
        return ids;
    }
}
