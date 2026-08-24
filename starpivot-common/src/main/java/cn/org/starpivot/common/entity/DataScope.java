package cn.org.starpivot.common.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据权限上下文，供 MyBatis 数据范围插件拼接 SQL 条件。
 * <p>
 * 多角色按并集解释：{@code all=true} 表示不加过滤；否则部门 ID 集合与
 * {@code includeSelf} 以 OR 组合。两者皆空表示无可见数据。
 * </p>
 */
@Data
@NoArgsConstructor
public class DataScope {

    /** 兼容旧实现的 SQL 片段；拦截器在未标记全部权限时仍可优先使用 */
    private String sqlFilter;

    /** 当前用户可见的部门 ID 列表 */
    private List<Long> deptIds;

    /** 当前用户 ID */
    private Long userId;

    /** 当前用户所属部门 ID */
    private Long userDeptId;

    /** 全部数据权限（超级管理员或角色 data_scope=1），查询不加部门条件 */
    private boolean all;

    /** 是否包含仅本人条件（与部门条件 OR） */
    private boolean includeSelf;

    /**
     * @param sqlFilter 数据范围 SQL 片段
     * @param deptIds   可见部门 ID 列表
     * @param userId    当前用户 ID
     */
    public DataScope(String sqlFilter, List<Long> deptIds, Long userId) {
        this.sqlFilter = sqlFilter;
        this.deptIds = deptIds;
        this.userId = userId;
    }

    /**
     * @param sqlFilter  数据范围 SQL 片段
     * @param deptIds    可见部门 ID 列表
     * @param userId     当前用户 ID
     * @param userDeptId 当前用户所属部门 ID
     */
    public DataScope(String sqlFilter, List<Long> deptIds, Long userId, Long userDeptId) {
        this.sqlFilter = sqlFilter;
        this.deptIds = deptIds;
        this.userId = userId;
        this.userDeptId = userDeptId;
    }

    /** 全部数据权限。 */
    public static DataScope all(Long userId, Long userDeptId) {
        DataScope scope = new DataScope();
        scope.setAll(true);
        scope.setUserId(userId);
        scope.setUserDeptId(userDeptId);
        return scope;
    }

    /** 无可见数据。 */
    public static DataScope none(Long userId) {
        return none(userId, null);
    }

    /** 无可见数据（保留用户部门信息）。 */
    public static DataScope none(Long userId, Long userDeptId) {
        DataScope scope = new DataScope();
        scope.setUserId(userId);
        scope.setUserDeptId(userDeptId);
        scope.setDeptIds(new ArrayList<>());
        return scope;
    }

    /**
     * 受限范围：部门集合与仅本人按并集生效。
     *
     * @param userId      当前用户 ID
     * @param userDeptId  当前用户部门 ID
     * @param deptIds     可见部门
     * @param includeSelf 是否包含本人记录
     */
    public static DataScope restricted(Long userId, Long userDeptId, List<Long> deptIds, boolean includeSelf) {
        DataScope scope = new DataScope();
        scope.setUserId(userId);
        scope.setUserDeptId(userDeptId);
        scope.setDeptIds(deptIds != null ? new ArrayList<>(deptIds) : new ArrayList<>());
        scope.setIncludeSelf(includeSelf);
        return scope;
    }
}
