package cn.org.starpivot.system.service;

import cn.org.starpivot.api.system.dto.AssigneeResolveRequest;

import java.util.List;

/**
 * 组织架构查询服务（供审批中台等模块解析审批人）。
 */
public interface OrgAssigneeService {

    List<Long> resolveAssignees(AssigneeResolveRequest request);

    String displayName(Long userId);
}
