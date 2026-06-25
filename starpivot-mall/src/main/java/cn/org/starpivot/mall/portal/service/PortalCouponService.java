package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.mall.portal.domain.bo.PortalCouponTrialBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCheckoutCouponVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalClaimableCouponVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalMemberCouponVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalMyCouponVo;

import java.math.BigDecimal;
import java.util.List;

/**
 * C 端优惠券：领取、试算、锁定、核销、释放。
 */
public interface PortalCouponService {

    List<PortalClaimableCouponVo> listClaimable(Long memberId);

    List<PortalMyCouponVo> listMine(Long memberId, Integer status);

    /** 领取优惠券，返回领取记录 ID */
    Long receive(Long memberId, Long couponId);

    List<PortalMemberCouponVo> listUsable(Long memberId, PortalCouponTrialBo trialBo);

    /** 结算页优惠券列表（含不可用原因） */
    List<PortalCheckoutCouponVo> listCheckoutCoupons(Long memberId, PortalCouponTrialBo trialBo);

    /** 计算优惠金额并校验适用范围 */
    BigDecimal calculateDiscount(Long memberId, Long couponHistoryId, PortalCouponTrialBo trialBo);

    /** 提交订单后锁定（写入 orderId，未核销） */
    void lockToOrder(Long couponHistoryId, Long memberId, Long orderId, String orderSn);

    /** 支付成功核销 */
    void confirmUsed(Long orderId);

    /** 未支付取消/超时释放 */
    void releaseByOrder(Long orderId);

    /** 根据领取记录解析优惠券 ID */
    Long resolveCouponId(Long couponHistoryId);
}
