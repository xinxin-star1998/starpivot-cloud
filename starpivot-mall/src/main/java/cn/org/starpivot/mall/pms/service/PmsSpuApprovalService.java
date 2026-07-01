package cn.org.starpivot.mall.pms.service;

/**
 * 商品上架审批接入。
 */
public interface PmsSpuApprovalService {

    void submitApproval(Long spuId);

    void handleApprovalFinished(String bizModule, String bizType, String bizKey, String result, String comment);
}
