package cn.org.starpivot.mall.common;

import cn.org.starpivot.api.approval.ApprovalInternalClient;
import cn.org.starpivot.api.approval.dto.InternalApprovalSubmitRequest;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 商城审批提交通用流程：本地标记待审 → 远程提交 → 绑定实例；失败时补偿撤回并回滚。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MallApprovalSubmitter {

    private final ApprovalInternalClient approvalInternalClient;
    private final TransactionTemplate transactionTemplate;

    public void submit(Runnable markPending, Supplier<InternalApprovalSubmitRequest> requestSupplier,
                       Consumer<Long> bindInstanceId, Runnable rollbackDraft) {
        transactionTemplate.executeWithoutResult(status -> markPending.run());

        Long instanceId = null;
        try {
            instanceId = submitRemote(requestSupplier.get());
            final Long boundInstanceId = instanceId;
            transactionTemplate.executeWithoutResult(status -> bindInstanceId.accept(boundInstanceId));
        } catch (RuntimeException ex) {
            compensateApproval(instanceId);
            transactionTemplate.executeWithoutResult(status -> rollbackDraft.run());
            if (ex instanceof BizException bizException) {
                throw bizException;
            }
            throw new BizException(ErrorCode.SYSTEM_ERROR, "提交审批失败: " + ex.getMessage());
        }
    }

    public Long requireUserId() {
        Long userId = SecurityContextUtils.getUserId();
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "未登录");
        }
        return userId;
    }

    private Long submitRemote(InternalApprovalSubmitRequest request) {
        Result<Long> result = approvalInternalClient.submit(request);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new BizException(result != null ? result.getMessage() : "提交审批失败");
        }
        return result.getData();
    }

    private void compensateApproval(Long instanceId) {
        if (instanceId == null) {
            return;
        }
        try {
            approvalInternalClient.withdraw(instanceId);
        } catch (Exception ex) {
            log.error("补偿撤回审批实例失败: instanceId={}", instanceId, ex);
        }
    }
}
