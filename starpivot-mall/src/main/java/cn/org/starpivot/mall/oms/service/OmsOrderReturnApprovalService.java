package cn.org.starpivot.mall.oms.service;

/**
 * 退货申请审批接入。
 */
public interface OmsOrderReturnApprovalService {

    /**
     * 提交退货审批。
     */
    void submitApproval(Long returnId);

    /**
     * 消费审批完结 MQ。
     */
    void handleApprovalFinished(String bizModule, String bizType, String bizKey, String result, String comment);
}
