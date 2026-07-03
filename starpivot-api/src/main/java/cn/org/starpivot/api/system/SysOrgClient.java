package cn.org.starpivot.api.system;

import cn.org.starpivot.api.fallback.SysOrgClientFallbackFactory;
import cn.org.starpivot.api.system.dto.AssigneeResolveRequest;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 组织架构查询 Feign 客户端（审批人解析等）。
 */
@FeignClient(
        name = "starpivot-system",
        contextId = "sysOrgClient",
        path = "/api/${starpivot.api.version:v1}",
        fallbackFactory = SysOrgClientFallbackFactory.class)
public interface SysOrgClient {

    @PostMapping("/internal/org/assignees/resolve")
    Result<List<Long>> resolveAssignees(@RequestBody AssigneeResolveRequest request);

    @GetMapping("/internal/org/user/{userId}/display-name")
    Result<String> displayName(@PathVariable("userId") Long userId);

    @PostMapping("/internal/org/users/display-names")
    Result<Map<Long, String>> displayNames(@RequestBody List<Long> userIds);
}
