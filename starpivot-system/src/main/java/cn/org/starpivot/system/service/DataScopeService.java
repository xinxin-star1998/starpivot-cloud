package cn.org.starpivot.system.service;

import cn.org.starpivot.common.entity.DataScope;
import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.system.domain.entity.SysDept;
import cn.org.starpivot.system.domain.entity.SysRole;
import cn.org.starpivot.system.domain.entity.SysUser;
import cn.org.starpivot.system.mapper.RoleDeptMapper;
import cn.org.starpivot.system.mapper.SysDeptMapper;
import cn.org.starpivot.system.mapper.SysRoleMapper;
import cn.org.starpivot.system.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 数据权限范围服务类。
 * <p>
 * 根据当前登录用户的角色数据范围（全部/自定义/本部门/本部门及子部门/仅本人）
 * 计算 MyBatis 查询可用的 SQL 过滤片段，供用户列表等接口的数据隔离。
 * </p>
 * <ul>
 *   <li>{@link Slf4j} — 日志记录</li>
 *   <li>{@link Service} — Spring 服务组件</li>
 * </ul>
 *
 * @see cn.org.starpivot.common.entity.DataScope
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataScopeService {

    private static final String SQL_NONE = "EMPTY_IN";
    private static final String SQL_ALL = "1=1";

    private static final Map<String, Integer> SCOPE_PRIORITY = Map.of(
            AppConstants.DataScope.ALL, 1,
            AppConstants.DataScope.CUSTOM, 2,
            AppConstants.DataScope.DEPT, 3,
            AppConstants.DataScope.DEPT_AND_CHILD, 4,
            AppConstants.DataScope.SELF, 5
    );

    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final RoleDeptMapper roleDeptMapper;

    /**
     * 计算当前登录用户的数据权限范围。
     *
     * @return 含 SQL 过滤条件、部门 ID 集合及用户 ID 的 {@link DataScope} 对象
     */
    public DataScope getCurrentUserDataScope() {
        Long userId = SecurityContextUtils.getUserId();
        if (userId == null) {
            return new DataScope(SQL_NONE, Collections.emptyList(), null, null);
        }

        SysUser user = userMapper.selectById(userId);
        Long userDeptId = user != null ? user.getDeptId() : null;
        List<SysRole> roleList = roleMapper.selectRolesByUserId(userId);

        if (isSuperAdmin(userId, roleList)) {
            return new DataScope(SQL_ALL, Collections.emptyList(), userId, userDeptId);
        }
        if (CollectionUtils.isEmpty(roleList)) {
            return new DataScope(SQL_NONE, Collections.emptyList(), userId, userDeptId);
        }

        ScopeResult result = calculateDataScope(roleList, userDeptId);
        String sqlFilter = buildSqlFilter(result.scopeType, userId, result.deptIds, userDeptId);
        Long scopeUserDeptId = AppConstants.DataScope.SELF.equals(result.scopeType) ? null : userDeptId;
        return new DataScope(sqlFilter, new ArrayList<>(result.deptIds), userId, scopeUserDeptId);
    }

    private ScopeResult calculateDataScope(List<SysRole> roleList, Long userDeptId) {
        String highestScope = AppConstants.DataScope.SELF;
        Set<Long> deptIdSet = new HashSet<>();

        for (SysRole role : roleList) {
            String scope = Optional.ofNullable(role.getDataScope()).map(String::trim).orElse(AppConstants.DataScope.SELF);
            if (AppConstants.DataScope.ALL.equals(scope)) {
                return new ScopeResult(AppConstants.DataScope.ALL, Collections.emptySet());
            }
            if (isHigherPriority(scope, highestScope)) {
                highestScope = scope;
                deptIdSet.clear();
                processScope(scope, role.getRoleId(), userDeptId, deptIdSet);
            } else if (scope.equals(highestScope) && AppConstants.DataScope.CUSTOM.equals(scope)) {
                addCustomDeptIds(role.getRoleId(), deptIdSet);
            }
        }
        return new ScopeResult(highestScope, deptIdSet);
    }

    private boolean isHigherPriority(String scope1, String scope2) {
        return SCOPE_PRIORITY.getOrDefault(scope1, 99) < SCOPE_PRIORITY.getOrDefault(scope2, 99);
    }

    private void processScope(String scope, Long roleId, Long userDeptId, Set<Long> deptIdSet) {
        switch (scope) {
            case AppConstants.DataScope.CUSTOM -> addCustomDeptIds(roleId, deptIdSet);
            case AppConstants.DataScope.DEPT_AND_CHILD -> {
                if (userDeptId != null) {
                    deptIdSet.addAll(selectDeptIdsByParentId(userDeptId));
                }
            }
            case AppConstants.DataScope.DEPT -> {
                if (userDeptId != null) {
                    deptIdSet.add(userDeptId);
                }
            }
            default -> { }
        }
    }

    private void addCustomDeptIds(Long roleId, Set<Long> deptIdSet) {
        List<Long> ids = roleDeptMapper.selectDeptIdsByRoleId(roleId);
        if (ids != null) {
            deptIdSet.addAll(ids);
        }
    }

    private boolean isSuperAdmin(Long userId, List<SysRole> roleList) {
        if (!AppConstants.ADMIN_USER_ID.equals(userId)) {
            return false;
        }
        return roleList.stream().anyMatch(role ->
                AppConstants.ADMIN_ROLE_KEY.equals(role.getRoleKey())
                        || AppConstants.DataScope.ALL.equals(role.getDataScope()));
    }

    private String buildSqlFilter(String dataScopeType, Long userId, Set<Long> deptIdSet, Long userDeptId) {
        return switch (dataScopeType) {
            case AppConstants.DataScope.ALL -> SQL_ALL;
            case AppConstants.DataScope.CUSTOM, AppConstants.DataScope.DEPT_AND_CHILD -> {
                if (CollectionUtils.isEmpty(deptIdSet)) {
                    yield SQL_NONE;
                }
                yield "u.dept_id IN (" + String.join(",", deptIdSet.stream().map(String::valueOf).toList()) + ")";
            }
            case AppConstants.DataScope.DEPT -> userDeptId == null ? SQL_NONE : "u.dept_id = #{param.userDeptId}";
            case AppConstants.DataScope.SELF -> "u.user_id = #{param.userId}";
            default -> SQL_NONE;
        };
    }

    private Set<Long> selectDeptIdsByParentId(Long parentId) {
        Set<Long> deptIdSet = new HashSet<>();
        if (parentId == null) {
            return deptIdSet;
        }
        deptIdSet.add(parentId);
        collectChildrenDeptIds(parentId, deptIdSet);
        return deptIdSet;
    }

    private void collectChildrenDeptIds(Long parentId, Set<Long> deptIdSet) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getParentId, parentId)
                .eq(SysDept::getDelFlag, AppConstants.DelFlag.NORMAL)
                .eq(SysDept::getStatus, AppConstants.Status.NORMAL);
        List<SysDept> children = deptMapper.selectList(wrapper);
        for (SysDept child : children) {
            deptIdSet.add(child.getDeptId());
            collectChildrenDeptIds(child.getDeptId(), deptIdSet);
        }
    }

    private record ScopeResult(String scopeType, Set<Long> deptIds) {
    }
}
