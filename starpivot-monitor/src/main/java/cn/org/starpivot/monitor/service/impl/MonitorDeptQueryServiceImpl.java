package cn.org.starpivot.monitor.service.impl;

import cn.org.starpivot.monitor.domain.entity.MonitorDept;
import cn.org.starpivot.monitor.mapper.MonitorDeptMapper;
import cn.org.starpivot.monitor.service.MonitorDeptQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * {@link MonitorDeptQueryService} 默认实现，委托 {@link MonitorDeptMapper} 查询。
 * <p>
 * {@link Service}：注册为 Spring 服务 Bean；
 * {@link RequiredArgsConstructor}：注入 {@link MonitorDeptMapper}。
 */
@Service
@RequiredArgsConstructor
public class MonitorDeptQueryServiceImpl implements MonitorDeptQueryService {

    private final MonitorDeptMapper monitorDeptMapper;

    /**
     * 按部门 ID 集合批量查询部门信息。
     * <p>传入 {@code null} 或空集合时直接返回空列表，不访问数据库。</p>
     *
     * @param deptIds 部门 ID 集合
     * @return {@link MonitorDept} 列表，无匹配时返回空列表
     */
    @Override
    public List<MonitorDept> listByIds(Collection<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return Collections.emptyList();
        }
        return monitorDeptMapper.selectByDeptIds(deptIds);
    }

    /**
     * 按部门 ID 查询单个部门信息。
     *
     * @param deptId 部门 ID，为 {@code null} 时返回 {@code null}
     * @return 匹配的 {@link MonitorDept}，不存在或 ID 为空时返回 {@code null}
     */
    @Override
    public MonitorDept getById(Long deptId) {
        return deptId == null ? null : monitorDeptMapper.selectByDeptId(deptId);
    }
}
