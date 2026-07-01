package cn.org.starpivot.mall.sms.service.impl;

import cn.org.starpivot.api.approval.dto.InternalApprovalSubmitRequest;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.common.MallApprovalConstants;
import cn.org.starpivot.mall.common.MallApprovalSubmitter;
import cn.org.starpivot.mall.common.MallAuditStatus;
import cn.org.starpivot.mall.sms.domain.bo.CouponSaveBo;
import cn.org.starpivot.mall.sms.entity.SmsCoupon;
import cn.org.starpivot.mall.sms.mapper.SmsCouponMapper;
import cn.org.starpivot.mall.sms.service.SmsCouponApprovalService;
import cn.org.starpivot.mall.sms.service.SmsCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsCouponApprovalServiceImpl implements SmsCouponApprovalService {

    private static final int PUBLISHED = 1;

    private final SmsCouponMapper smsCouponMapper;
    private final SmsCouponService smsCouponService;
    private final MallApprovalSubmitter mallApprovalSubmitter;

    @Override
    public void submitApproval(Long couponId) {
        SmsCoupon coupon = validateForSubmit(couponId);
        Long userId = mallApprovalSubmitter.requireUserId();
        mallApprovalSubmitter.submit(
                () -> markPending(couponId),
                () -> buildSubmitRequest(coupon, userId),
                instanceId -> bindInstanceId(couponId, instanceId),
                () -> rollbackDraft(couponId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleApprovalFinished(String bizModule, String bizType, String bizKey, String result, String comment) {
        if (!MallApprovalConstants.BIZ_MODULE.equals(bizModule)
                || !MallApprovalConstants.BIZ_TYPE_COUPON.equals(bizType)) {
            return;
        }
        Long couponId = MallApprovalConstants.parseCouponId(bizKey);
        if (couponId == null) {
            log.warn("无法解析优惠券 bizKey: {}", bizKey);
            return;
        }
        SmsCoupon coupon = smsCouponMapper.selectById(couponId);
        if (coupon == null) {
            log.warn("审批完结但优惠券不存在: {}", couponId);
            return;
        }
        if (!MallAuditStatus.PENDING.equals(coupon.getAuditStatus())) {
            log.info("跳过非待审状态的优惠券完结事件: id={}, auditStatus={}, result={}",
                    couponId, coupon.getAuditStatus(), result);
            return;
        }

        SmsCoupon patch = new SmsCoupon();
        patch.setId(couponId);
        switch (result) {
            case MallAuditStatus.APPROVED -> {
                patch.setAuditStatus(MallAuditStatus.APPROVED);
                patch.setPublish(PUBLISHED);
            }
            case MallAuditStatus.REJECTED -> patch.setAuditStatus(MallAuditStatus.REJECTED);
            case MallAuditStatus.WITHDRAWN -> patch.setAuditStatus(MallAuditStatus.WITHDRAWN);
            default -> {
                log.warn("未知审批结果: {}", result);
                return;
            }
        }
        smsCouponMapper.updateById(patch);
        log.info("优惠券审批完结: id={}, result={}, comment={}", couponId, result, comment);
    }

    private SmsCoupon validateForSubmit(Long couponId) {
        SmsCoupon coupon = smsCouponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BizException("优惠券不存在");
        }
        if (Objects.equals(coupon.getPublish(), PUBLISHED)) {
            throw new BizException("优惠券已发布，无需重复提交审批");
        }
        if (!MallAuditStatus.canSubmit(coupon.getAuditStatus())) {
            throw new BizException("当前审批状态不可重复提交");
        }
        smsCouponService.assertPublishable(couponId);
        return coupon;
    }

    private void markPending(Long couponId) {
        SmsCoupon patch = new SmsCoupon();
        patch.setId(couponId);
        patch.setAuditStatus(MallAuditStatus.PENDING);
        smsCouponMapper.updateById(patch);
    }

    private InternalApprovalSubmitRequest buildSubmitRequest(SmsCoupon coupon, Long userId) {
        InternalApprovalSubmitRequest request = new InternalApprovalSubmitRequest();
        request.setBizModule(MallApprovalConstants.BIZ_MODULE);
        request.setBizType(MallApprovalConstants.BIZ_TYPE_COUPON);
        request.setBizKey(MallApprovalConstants.couponBizKey(coupon.getId()));
        request.setTitle("优惠券发布审批 #" + coupon.getId() + " · " + coupon.getCouponName());
        request.setStarterId(userId);
        Map<String, Object> context = new HashMap<>();
        context.put("amount", coupon.getAmount());
        context.put("publishCount", coupon.getPublishCount());
        context.put("useType", coupon.getUseType());
        request.setContext(context);
        return request;
    }

    private void bindInstanceId(Long couponId, Long instanceId) {
        SmsCoupon patch = new SmsCoupon();
        patch.setId(couponId);
        patch.setApprovalInstanceId(instanceId);
        patch.setAuditStatus(MallAuditStatus.PENDING);
        smsCouponMapper.updateById(patch);
    }

    private void rollbackDraft(Long couponId) {
        SmsCoupon rollback = new SmsCoupon();
        rollback.setId(couponId);
        rollback.setAuditStatus(MallAuditStatus.DRAFT);
        smsCouponMapper.updateById(rollback);
    }
}
