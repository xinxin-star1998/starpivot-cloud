package cn.org.starpivot.mall.wms.service;

import cn.org.starpivot.mall.wms.entity.WmsPurchase;

/**
 * 采购单审批接入。
 */
public interface WmsPurchaseApprovalService {

    /**
     * 提交采购单审批。
     */
    void submitApproval(Long purchaseId);

    /**
     * 消费审批完结 MQ。
     */
    void handleApprovalFinished(String bizModule, String bizType, String bizKey, String result, String comment);

    /**
     * 审批实例已完结但本地 auditStatus 仍为 PENDING 时补偿同步（如 MQ 未投递）。
     */
    void reconcileIfStale(WmsPurchase purchase);
}
