package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.mall.portal.domain.bo.PortalOrderItemBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderPriceTrialBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderPriceTrialVo;

import java.util.List;

/**
 * C 端订单计价：秒杀价、会员价、满减、阶梯、优惠券、积分、运费。
 */
public interface PortalOrderPricingService {

    /** 结算页试算（不写库） */
    PortalOrderPriceTrialVo trial(Long memberId, PortalOrderPriceTrialBo bo);

    /**
     * 下单计价（与 submit 共用逻辑）。
     *
     * @param validateIntegration true 时校验积分余额并抛业务异常
     */
    PortalOrderPricingResult calculate(
            Long memberId,
            List<PortalOrderItemBo> orderItems,
            Long couponHistoryId,
            Integer useIntegration,
            boolean validateIntegration);

}
