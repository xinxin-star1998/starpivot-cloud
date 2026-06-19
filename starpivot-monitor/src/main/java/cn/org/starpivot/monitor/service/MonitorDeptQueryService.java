package cn.org.starpivot.monitor.service;

import cn.org.starpivot.monitor.domain.entity.MonitorDept;

import java.util.Collection;
import java.util.List;

public interface MonitorDeptQueryService {

    List<MonitorDept> listByIds(Collection<Long> deptIds);

    MonitorDept getById(Long deptId);
}
