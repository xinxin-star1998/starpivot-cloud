package cn.org.starpivot.common.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据权限上下文，供 MyBatis 数据范围插件拼接 SQL 条件。
 * <p>
 * 包含部门 ID 集合、用户标识及可选的 SQL 片段，用于按角色数据范围过滤查询结果。
 * </p>
 */
@Data
@NoArgsConstructor
public class DataScope {

    /** 数据范围 SQL 片段（如 {@code AND dept_id IN (...)}） */
    private String sqlFilter;

    /** 当前用户可见的部门 ID 列表 */
    private List<Long> deptIds;

    /** 当前用户 ID */
    private Long userId;

    /** 当前用户所属部门 ID */
    private Long userDeptId;

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
}
