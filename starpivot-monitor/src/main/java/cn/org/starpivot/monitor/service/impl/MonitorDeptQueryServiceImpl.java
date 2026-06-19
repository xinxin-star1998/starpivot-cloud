package cn.org.starpivot.monitor.service.impl;

import cn.org.starpivot.monitor.domain.entity.MonitorDept;
import cn.org.starpivot.monitor.mapper.MonitorDeptMapper;
import cn.org.starpivot.monitor.service.MonitorDeptQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonitorDeptQueryServiceImpl implements MonitorDeptQueryService {

    private final MonitorDeptMapper monitorDeptMapper;

    @Override
    public List<MonitorDept> listByIds(Collection<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return Collections.emptyList();
        }
        return monitorDeptMapper.selectByDeptIds(deptIds);
    }

    @Override
    public MonitorDept getById(Long deptId) {
        return deptId == null ? null : monitorDeptMapper.selectByDeptId(deptId);
    }
}
