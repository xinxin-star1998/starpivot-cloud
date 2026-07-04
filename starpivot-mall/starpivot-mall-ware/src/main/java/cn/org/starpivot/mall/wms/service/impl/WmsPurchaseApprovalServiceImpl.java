package cn.org.starpivot.mall.wms.service.impl;

import cn.org.starpivot.api.approval.ApprovalInternalClient;
import cn.org.starpivot.api.approval.dto.InternalApprovalSubmitRequest;
import cn.org.starpivot.api.approval.vo.ApprovalTimelineVo;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.common.MallApprovalConstants;
import cn.org.starpivot.mall.common.MallApprovalSubmitter;
import cn.org.starpivot.mall.common.MallAuditStatus;
import cn.org.starpivot.mall.wms.entity.WmsPurchase;
import cn.org.starpivot.mall.wms.entity.WmsPurchaseDetail;
import cn.org.starpivot.mall.wms.enums.WmsPurchaseStatusEnum;
import cn.org.starpivot.mall.wms.mapper.WmsPurchaseDetailMapper;
import cn.org.starpivot.mall.wms.mapper.WmsPurchaseMapper;
import cn.org.starpivot.mall.wms.service.WmsPurchaseApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WmsPurchaseApprovalServiceImpl implements WmsPurchaseApprovalService {

    private final WmsPurchaseMapper purchaseMapper;
    private final WmsPurchaseDetailMapper purchaseDetailMapper;
    private final MallApprovalSubmitter mallApprovalSubmitter;
    private final ApprovalInternalClient approvalInternalClient;

    @Override
    public void submitApproval(Long purchaseId) {
        WmsPurchase purchase = validateForSubmit(purchaseId);
        Long userId = mallApprovalSubmitter.requireUserId();
        mallApprovalSubmitter.submit(
                () -> markPending(purchaseId),
                () -> buildSubmitRequest(purchase, userId),
                instanceId -> bindInstanceId(purchaseId, instanceId),
                () -> rollbackDraft(purchaseId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleApprovalFinished(String bizModule, String bizType, String bizKey, String result, String comment) {
        applyApprovalFinished(bizModule, bizType, bizKey, result, comment);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void reconcileIfStale(WmsPurchase purchase) {
        if (purchase == null
                || purchase.getApprovalInstanceId() == null
                || !MallAuditStatus.PENDING.equals(purchase.getAuditStatus())) {
            return;
        }
        Result<ApprovalTimelineVo> result = approvalInternalClient.timeline(purchase.getApprovalInstanceId());
        if (result == null || !result.isSuccess() || result.getData() == null) {
            return;
        }
        String instanceStatus = result.getData().getStatus();
        if (instanceStatus == null || instanceStatus.isBlank() || "RUNNING".equals(instanceStatus)) {
            return;
        }
        applyApprovalFinished(
                MallApprovalConstants.BIZ_MODULE,
                MallApprovalConstants.BIZ_TYPE_PURCHASE,
                MallApprovalConstants.purchaseBizKey(purchase.getId()),
                instanceStatus,
                null);
    }

    private void applyApprovalFinished(String bizModule, String bizType, String bizKey, String result, String comment) {
        if (!MallApprovalConstants.BIZ_MODULE.equals(bizModule)
                || !MallApprovalConstants.BIZ_TYPE_PURCHASE.equals(bizType)) {
            return;
        }
        Long purchaseId = MallApprovalConstants.parsePurchaseId(bizKey);
        if (purchaseId == null) {
            log.warn("无法解析采购单 bizKey: {}", bizKey);
            return;
        }
        WmsPurchase purchase = purchaseMapper.selectById(purchaseId);
        if (purchase == null) {
            log.warn("审批完结但采购单不存在: {}", purchaseId);
            return;
        }
        if (!MallAuditStatus.PENDING.equals(purchase.getAuditStatus())) {
            log.info("跳过非待审状态的采购单完结事件: id={}, auditStatus={}, result={}",
                    purchaseId, purchase.getAuditStatus(), result);
            return;
        }

        String auditStatus = switch (result) {
            case MallAuditStatus.APPROVED -> MallAuditStatus.APPROVED;
            case MallAuditStatus.REJECTED -> MallAuditStatus.REJECTED;
            case MallAuditStatus.WITHDRAWN -> MallAuditStatus.WITHDRAWN;
            default -> null;
        };
        if (auditStatus == null) {
            log.warn("未知审批结果: {}", result);
            return;
        }

        WmsPurchase patch = new WmsPurchase();
        patch.setId(purchaseId);
        patch.setAuditStatus(auditStatus);
        patch.setUpdateTime(LocalDateTime.now());
        purchaseMapper.updateById(patch);
        log.info("采购单审批完结: id={}, result={}, comment={}", purchaseId, result, comment);
    }

    private WmsPurchase validateForSubmit(Long purchaseId) {
        WmsPurchase purchase = purchaseMapper.selectById(purchaseId);
        if (purchase == null) {
            throw new BizException("采购单不存在");
        }
        if (!WmsPurchaseStatusEnum.canMerge(purchase.getStatus())
                && !Integer.valueOf(WmsPurchaseStatusEnum.ASSIGNED.getCode()).equals(purchase.getStatus())) {
            throw new BizException("仅新建或已分配状态的采购单可提交审批");
        }
        if (!MallAuditStatus.canSubmit(purchase.getAuditStatus())) {
            throw new BizException("当前审批状态不可重复提交");
        }
        List<WmsPurchaseDetail> details = purchaseDetailMapper.listByPurchaseId(purchaseId);
        if (CollectionUtils.isEmpty(details)) {
            throw new BizException("采购单无明细，无法提交审批");
        }
        BigDecimal amount = purchase.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("采购金额无效，请先合并采购需求");
        }
        return purchase;
    }

    private void markPending(Long purchaseId) {
        WmsPurchase patch = new WmsPurchase();
        patch.setId(purchaseId);
        patch.setAuditStatus(MallAuditStatus.PENDING);
        patch.setUpdateTime(LocalDateTime.now());
        purchaseMapper.updateById(patch);
    }

    private InternalApprovalSubmitRequest buildSubmitRequest(WmsPurchase purchase, Long userId) {
        InternalApprovalSubmitRequest request = new InternalApprovalSubmitRequest();
        request.setBizModule(MallApprovalConstants.BIZ_MODULE);
        request.setBizType(MallApprovalConstants.BIZ_TYPE_PURCHASE);
        request.setBizKey(MallApprovalConstants.purchaseBizKey(purchase.getId()));
        request.setTitle("采购单审批 #" + purchase.getId());
        request.setStarterId(userId);
        Map<String, Object> context = new HashMap<>();
        context.put("amount", purchase.getAmount());
        context.put("wareId", purchase.getWareId());
        request.setContext(context);
        return request;
    }

    private void bindInstanceId(Long purchaseId, Long instanceId) {
        WmsPurchase patch = new WmsPurchase();
        patch.setId(purchaseId);
        patch.setApprovalInstanceId(instanceId);
        patch.setAuditStatus(MallAuditStatus.PENDING);
        patch.setUpdateTime(LocalDateTime.now());
        purchaseMapper.updateById(patch);
    }

    private void rollbackDraft(Long purchaseId) {
        WmsPurchase rollback = new WmsPurchase();
        rollback.setId(purchaseId);
        rollback.setAuditStatus(MallAuditStatus.DRAFT);
        rollback.setUpdateTime(LocalDateTime.now());
        purchaseMapper.updateById(rollback);
    }
}
