package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.api.approval.dto.InternalApprovalSubmitRequest;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.common.MallApprovalConstants;
import cn.org.starpivot.mall.common.MallApprovalSubmitter;
import cn.org.starpivot.mall.common.MallAuditStatus;
import cn.org.starpivot.mall.oms.OmsConstants;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;
import cn.org.starpivot.mall.oms.mapper.OmsOrderReturnApplyMapper;
import cn.org.starpivot.mall.oms.service.OmsOrderReturnApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OmsOrderReturnApprovalServiceImpl implements OmsOrderReturnApprovalService {

    private final OmsOrderReturnApplyMapper returnApplyMapper;
    private final MallApprovalSubmitter mallApprovalSubmitter;

    @Override
    public void submitApproval(Long returnId) {
        OmsOrderReturnApply apply = validateForSubmit(returnId);
        Long userId = mallApprovalSubmitter.requireUserId();
        mallApprovalSubmitter.submit(
                () -> markPending(returnId),
                () -> buildSubmitRequest(apply, userId),
                instanceId -> bindInstanceId(returnId, instanceId),
                () -> rollbackDraft(returnId));
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

    private InternalApprovalSubmitRequest buildSubmitRequest(OmsOrderReturnApply apply, Long userId) {
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
        return request;
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
}
