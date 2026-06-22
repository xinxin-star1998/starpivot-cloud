package cn.org.starpivot.monitor.service;

import cn.org.starpivot.monitor.domain.entity.MonitorDept;

import java.util.Collection;
import java.util.List;

/**
 * 监控模块部门只读查询服务接口。
 */
public interface MonitorDeptQueryService {

    /**
     * 按部门 ID 集合批量查询部门。
     *
     * @param deptIds 部门 ID 集合，空集合时返回空列表
     * @return 部门列表
     */
    List<MonitorDept> listByIds(Collection<Long> deptIds);

    /**
     * 按部门 ID 查询单个部门。
     *
     * @param deptId 部门 ID
     * @return 部门信息，ID 为空或不存在时返回 {@code null}
     */
    MonitorDept getById(Long deptId);
}
