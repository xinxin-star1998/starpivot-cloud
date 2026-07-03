package cn.org.starpivot.api.fallback;

import cn.org.starpivot.api.system.SysOrgClient;
import cn.org.starpivot.api.system.dto.AssigneeResolveRequest;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SysOrgClientFallbackFactory implements FallbackFactory<SysOrgClient> {

    private static final String ACTION = "组织架构";

    @Override
    public SysOrgClient create(Throwable cause) {
        return new SysOrgClient() {
            @Override
            public Result<List<Long>> resolveAssignees(AssigneeResolveRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<String> displayName(Long userId) {
                return Result.success(String.valueOf(userId));
            }

            @Override
            public Result<Map<Long, String>> displayNames(List<Long> userIds) {
                if (userIds == null || userIds.isEmpty()) {
                    return Result.success(Collections.emptyMap());
                }
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }
        };
    }
}
