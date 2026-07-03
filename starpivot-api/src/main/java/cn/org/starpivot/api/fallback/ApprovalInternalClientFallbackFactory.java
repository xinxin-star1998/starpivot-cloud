package cn.org.starpivot.api.fallback;

import cn.org.starpivot.api.approval.ApprovalInternalClient;
import cn.org.starpivot.api.approval.dto.InternalApprovalSubmitRequest;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FallbackFactory;

public class ApprovalInternalClientFallbackFactory implements FallbackFactory<ApprovalInternalClient> {

    private static final String ACTION = "审批服务";

    @Override
    public ApprovalInternalClient create(Throwable cause) {
        return new ApprovalInternalClient() {
            @Override
            public Result<Long> submit(InternalApprovalSubmitRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Void> withdraw(Long instanceId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }
        };
    }
}
