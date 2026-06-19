package cn.org.starpivot.common.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DataScope {
    private String sqlFilter;
    private List<Long> deptIds;
    private Long userId;
    private Long userDeptId;

    public DataScope(String sqlFilter, List<Long> deptIds, Long userId) {
        this.sqlFilter = sqlFilter;
        this.deptIds = deptIds;
        this.userId = userId;
    }

    public DataScope(String sqlFilter, List<Long> deptIds, Long userId, Long userDeptId) {
        this.sqlFilter = sqlFilter;
        this.deptIds = deptIds;
        this.userId = userId;
        this.userDeptId = userDeptId;
    }
}
