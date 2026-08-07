package cn.org.starpivot.system.datascope;

import cn.org.starpivot.common.datascope.DataScopeProvider;
import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.entity.DataScope;
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
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统模块数据权限提供者实现。
 * <p>
 * 查询 {@code sys_role} 的 {@code data_scope} 字段和 {@code sys_role_dept} 关联表，
 * 构建完整的 {@link DataScope} 上下文供数据权限拦截器使用。
 * </p>
 * <p>数据范围策略：</p>
 * <ul>
 *   <li>{@code 1} — 全部数据权限：返回空 sqlFilter</li>
 *   <li>{@code 2} — 自定义数据权限：返回 {@code sys_role_dept} 配置的部门 ID 列表</li>
 *   <li>{@code 3} — 本部门：返回用户所属部门 ID</li>
 *   <li>{@code 4} — 本部门及子部门：返回用户所属部门及其所有子孙部门 ID</li>
 *   <li>{@code 5} — 仅本人：返回空 deptIds，由拦截器使用 userAlias 过滤</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemDataScopeProvider implements DataScopeProvider {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final RoleDeptMapper roleDeptMapper;
    private final SysDeptMapper sysDeptMapper;

    @Override
    public DataScope resolve(Long userId) {
        if (userId == null) {
            return new DataScope();
        }

        // 获取用户信息（含 deptId）
        SysUser user = sysUserMapper.selectById(userId);
        Long userDeptId = (user != null) ? user.getDeptId() : null;

        // 获取用户角色列表
        List<SysRole> roles = sysRoleMapper.selectRoleListByUserId(userId);
        if (roles == null || roles.isEmpty()) {
            // 无角色时返回仅本人权限
            return buildSelfOnlyScope(userId);
        }

        // 计算最宽松的数据范围（数字越小权限越大）
        String maxDataScope = null;
        List<Long> allCustomDeptIds = new ArrayList<>();
        boolean hasAllScope = false;

        for (SysRole role : roles) {
            // 跳过已停用的角色
            if (!AppConstants.Status.NORMAL.equals(role.getStatus())) {
                continue;
            }
            String scope = role.getDataScope();
            if (scope == null) {
                continue;
            }

            // 全部数据权限：直接返回
            if (AppConstants.DataScope.ALL.equals(scope)) {
                hasAllScope = true;
                break;
            }

            // 自定义数据权限：累积部门 ID
            if (AppConstants.DataScope.CUSTOM.equals(scope)) {
                List<Long> customDeptIds = roleDeptMapper.selectDeptIdsByRoleId(role.getRoleId());
                if (customDeptIds != null) {
                    allCustomDeptIds.addAll(customDeptIds);
                }
            }

            // 记录最宽松的范围
            if (maxDataScope == null || scope.compareTo(maxDataScope) < 0) {
                maxDataScope = scope;
            }
        }

        // 全部数据权限
        if (hasAllScope) {
            return new DataScope(null, null, userId, userDeptId);
        }

        // 根据最宽松范围构建 DataScope
        if (maxDataScope == null) {
            return buildSelfOnlyScope(userId);
        }

        switch (maxDataScope) {
            case AppConstants.DataScope.CUSTOM:
                // 自定义数据权限
                if (allCustomDeptIds.isEmpty()) {
                    return buildSelfOnlyScope(userId);
                }
                return new DataScope(null, distinctList(allCustomDeptIds), userId, userDeptId);

            case AppConstants.DataScope.DEPT:
                // 本部门
                if (userDeptId == null) {
                    return buildSelfOnlyScope(userId);
                }
                return new DataScope(null, Collections.singletonList(userDeptId), userId, userDeptId);

            case AppConstants.DataScope.DEPT_AND_CHILD:
                // 本部门及子部门
                if (userDeptId == null) {
                    return buildSelfOnlyScope(userId);
                }
                List<Long> deptAndChildIds = getDeptAndChildIds(userDeptId);
                return new DataScope(null, deptAndChildIds, userId, userDeptId);

            case AppConstants.DataScope.SELF:
            default:
                // 仅本人
                return buildSelfOnlyScope(userId);
        }
    }

    /**
     * 构建仅本人权限的 DataScope。
     */
    private DataScope buildSelfOnlyScope(Long userId) {
        DataScope scope = new DataScope();
        scope.setUserId(userId);
        // sqlFilter 留空，由拦截器根据 userAlias 构建
        return scope;
    }

    /**
     * 获取指定部门及其所有子孙部门 ID。
     * <p>
     * 利用 {@code ancestors} 字段的 LIKE 查询实现（如 ancestors = "0,1,100"）。
     * </p>
     *
     * @param deptId 部门 ID
     * @return 包含本部门及所有子孙部门的 ID 列表
     */
    private List<Long> getDeptAndChildIds(Long deptId) {
        Set<Long> result = new HashSet<>();
        result.add(deptId);

        // 查询本部门信息获取 ancestors
        SysDept dept = sysDeptMapper.selectById(deptId);
        if (dept == null) {
            return new ArrayList<>(result);
        }

        String ancestors = dept.getAncestors();
        if (ancestors == null || ancestors.isEmpty()) {
            return new ArrayList<>(result);
        }

        // 查询所有 ancestors 以当前部门路径开头的部门（即子孙部门）
        // ancestors 格式如 "0,1,100"，子孙部门的 ancestors 会是 "0,1,100,101,..."
        String pattern = ancestors + "," + deptId + ",%";
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(SysDept::getAncestors, ancestors + "," + deptId);
        wrapper.eq(SysDept::getStatus, AppConstants.Status.NORMAL);
        List<SysDept> childDepts = sysDeptMapper.selectList(wrapper);
        for (SysDept child : childDepts) {
            result.add(child.getDeptId());
        }

        return new ArrayList<>(result);
    }

    /**
     * 去重列表。
     */
    private List<Long> distinctList(List<Long> list) {
        return list.stream().distinct().collect(Collectors.toList());
    }
}
