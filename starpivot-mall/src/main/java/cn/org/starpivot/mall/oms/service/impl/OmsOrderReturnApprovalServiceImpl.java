package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.api.approval.ApprovalInternalClient;
import cn.org.starpivot.api.approval.dto.InternalApprovalSubmitRequest;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.mall.common.MallApprovalConstants;
import cn.org.starpivot.mall.common.MallAuditStatus;
import cn.org.starpivot.mall.oms.OmsConstants;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;
import cn.org.starpivot.mall.oms.mapper.OmsOrderReturnApplyMapper;
import cn.org.starpivot.mall.oms.service.OmsOrderReturnApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OmsOrderReturnApprovalServiceImpl implements OmsOrderReturnApprovalService {

    private final OmsOrderReturnApplyMapper returnApplyMapper;
    private final ApprovalInternalClient approvalInternalClient;
    private final TransactionTemplate transactionTemplate;

    @Override
    public void submitApproval(Long returnId) {
        OmsOrderReturnApply apply = validateForSubmit(returnId);
        Long userId = requireUserId();

        transactionTemplate.executeWithoutResult(status -> markPending(returnId));

        Long instanceId = null;
        try {
            instanceId = submitRemote(apply, userId);
            final Long boundInstanceId = instanceId;
            transactionTemplate.executeWithoutResult(status -> bindInstanceId(returnId, boundInstanceId));
        } catch (RuntimeException ex) {
            compensateApproval(instanceId);
            transactionTemplate.executeWithoutResult(status -> rollbackDraft(returnId));
            if (ex instanceof BizException bizException) {
                throw bizException;
            }
            throw new BizException(ErrorCode.SYSTEM_ERROR, "提交审批失败: " + ex.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleApprovalFinished(String bizModule, String bizType, String bizKey, String result, String comment) {
        if (!MallApprovalConstants.BIZ_MODULE.equals(bizModule)
                || !MallApprovalConstants.BIZ_TYPE_RETURN.equals(bizType)) {
            return;
        }
        Long returnId = MallApprovalConstants.parseReturnId(bizKey);
        if (returnId == null) {
            log.warn("无法解析退货 bizKey: {}", bizKey);
            return;
        }
        OmsOrderReturnApply apply = returnApplyMapper.selectById(returnId);
        if (apply == null) {
            log.warn("审批完结但退货申请不存在: {}", returnId);
            return;
        }
        if (!MallAuditStatus.PENDING.equals(apply.getAuditStatus())) {
            log.info("跳过非待审状态的退货完结事件: id={}, auditStatus={}, result={}",
                    returnId, apply.getAuditStatus(), result);
            return;
        }

        OmsOrderReturnApply patch = new OmsOrderReturnApply();
        patch.setId(returnId);
        patch.setHandleTime(LocalDateTime.now());
        if (comment != null && !comment.isBlank()) {
            patch.setHandleNote(comment);
        }

        switch (result) {
            case MallAuditStatus.APPROVED -> {
                patch.setAuditStatus(MallAuditStatus.APPROVED);
                patch.setStatus(OmsConstants.RETURN_STATUS_RETURNING);
            }
            case MallAuditStatus.REJECTED -> {
                patch.setAuditStatus(MallAuditStatus.REJECTED);
                patch.setStatus(OmsConstants.RETURN_STATUS_REJECTED);
            }
            case MallAuditStatus.WITHDRAWN -> patch.setAuditStatus(MallAuditStatus.WITHDRAWN);
            default -> {
                log.warn("未知审批结果: {}", result);
                return;
            }
        }

        returnApplyMapper.updateById(patch);
        log.info("退货审批完结: id={}, result={}, comment={}", returnId, result, comment);
    }

    private OmsOrderReturnApply validateForSubmit(Long returnId) {
        OmsOrderReturnApply apply = returnApplyMapper.selectById(returnId);
        if (apply == null) {
            throw new BizException("退货申请不存在");
        }
        if (!Integer.valueOf(OmsConstants.RETURN_STATUS_PENDING).equals(apply.getStatus())) {
            throw new BizException("仅待处理状态的退货申请可提交审批");
        }
        if (!MallAuditStatus.canSubmit(apply.getAuditStatus())) {
            throw new BizException("当前审批状态不可重复提交");
        }
        return apply;
    }

    private void markPending(Long returnId) {
        OmsOrderReturnApply patch = new OmsOrderReturnApply();
        patch.setId(returnId);
        patch.setAuditStatus(MallAuditStatus.PENDING);
        returnApplyMapper.updateById(patch);
    }

    private Long submitRemote(OmsOrderReturnApply apply, Long userId) {
        InternalApprovalSubmitRequest request = new InternalApprovalSubmitRequest();
        request.setBizModule(MallApprovalConstants.BIZ_MODULE);
        request.setBizType(MallApprovalConstants.BIZ_TYPE_RETURN);
        request.setBizKey(MallApprovalConstants.returnBizKey(apply.getId()));
        request.setTitle("退货审批 #" + apply.getId() + " · " + apply.getOrderSn());
        request.setStarterId(userId);
        Map<String, Object> context = new HashMap<>();
        context.put("returnAmount", apply.getReturnAmount());
        context.put("orderSn", apply.getOrderSn());
        context.put("skuId", apply.getSkuId());
        context.put("skuName", apply.getSkuName());
        request.setContext(context);

        Result<Long> result = approvalInternalClient.submit(request);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new BizException(result != null ? result.getMessage() : "提交审批失败");
        }
        return result.getData();
    }

    private void bindInstanceId(Long returnId, Long instanceId) {
        OmsOrderReturnApply patch = new OmsOrderReturnApply();
        patch.setId(returnId);
        patch.setApprovalInstanceId(instanceId);
        patch.setAuditStatus(MallAuditStatus.PENDING);
        returnApplyMapper.updateById(patch);
    }

    private void rollbackDraft(Long returnId) {
        OmsOrderReturnApply rollback = new OmsOrderReturnApply();
        rollback.setId(returnId);
        rollback.setAuditStatus(MallAuditStatus.DRAFT);
        returnApplyMapper.updateById(rollback);
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

    private Long requireUserId() {
        Long userId = SecurityContextUtils.getUserId();
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "未登录");
        }
        return userId;
    }
}
