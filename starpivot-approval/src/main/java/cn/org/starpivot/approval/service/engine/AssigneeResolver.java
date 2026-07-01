package cn.org.starpivot.approval.service.engine;

import cn.org.starpivot.api.system.SysOrgClient;
import cn.org.starpivot.api.system.dto.AssigneeResolveRequest;
import cn.org.starpivot.approval.domain.entity.ApTemplateStep;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 审批人解析（Feign 调 starpivot-system 组织数据）。
 */
@Component
@RequiredArgsConstructor
public class AssigneeResolver {

    private final SysOrgClient sysOrgClient;

    public List<Long> resolve(ApTemplateStep step, Long starterId) {
        if (step == null || !StringUtils.hasText(step.getAssigneeType())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "审批人策略不能为空");
        }
        AssigneeResolveRequest request = new AssigneeResolveRequest();
        request.setAssigneeType(step.getAssigneeType());
        request.setAssigneeValue(step.getAssigneeValue());
        request.setStarterId(starterId);
        Result<List<Long>> result = sysOrgClient.resolveAssignees(request);
        if (result == null || !result.isSuccess() || result.getData() == null || result.getData().isEmpty()) {
            throw new BizException(result != null ? result.getMessage() : "解析审批人失败");
        }
        return result.getData();
    }

    public String displayName(Long userId) {
        if (userId == null) {
            return "";
        }
        return displayNames(List.of(userId)).getOrDefault(userId, String.valueOf(userId));
    }

    public Map<Long, String> displayNames(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<Long> distinct = userIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (distinct.isEmpty()) {
            return Map.of();
        }
        Result<Map<Long, String>> result = sysOrgClient.displayNames(distinct);
        if (result != null && result.isSuccess() && result.getData() != null) {
            Map<Long, String> names = new HashMap<>(result.getData());
            for (Long userId : distinct) {
                names.putIfAbsent(userId, String.valueOf(userId));
            }
            return names;
        }
        Map<Long, String> fallback = new HashMap<>();
        for (Long userId : distinct) {
            fallback.put(userId, String.valueOf(userId));
        }
        return fallback;
    }
}
