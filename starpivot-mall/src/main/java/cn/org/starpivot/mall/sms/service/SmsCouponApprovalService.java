package cn.org.starpivot.mall.sms.service;

/**
 * 优惠券审批接入。
 */
public interface SmsCouponApprovalService {

    void submitApproval(Long couponId);

    void handleApprovalFinished(String bizModule, String bizType, String bizKey, String result, String comment);
}
