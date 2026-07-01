package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderItemBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderSubmitBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalSeckillOrderBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderSubmitVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalSeckillPageVo;
import cn.org.starpivot.mall.portal.service.PortalOrderService;
import cn.org.starpivot.mall.portal.service.PortalSeckillRateLimitService;
import cn.org.starpivot.mall.portal.service.PortalSeckillService;
import cn.org.starpivot.mall.portal.service.PortalSeckillStockService;
import cn.org.starpivot.mall.portal.service.support.PortalHomeMarketingLoader;
import cn.org.starpivot.mall.portal.service.support.PortalSeckillSessionSupport;
import cn.org.starpivot.mall.portal.service.support.PortalSeckillSessionSupport.SeckillContext;
import cn.org.starpivot.mall.sms.entity.SmsSeckillSkuRelation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortalSeckillServiceImpl implements PortalSeckillService {

    private final PortalHomeMarketingLoader portalHomeMarketingLoader;
    private final PortalSeckillSessionSupport portalSeckillSessionSupport;
    private final PortalSeckillRateLimitService portalSeckillRateLimitService;
    private final PortalSeckillStockService portalSeckillStockService;
    private final PortalOrderService portalOrderService;

    @Override
    @Transactional(readOnly = true)
    public PortalSeckillPageVo getPage(Long sessionId) {
        return portalHomeMarketingLoader.loadSeckillPage(sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PortalOrderSubmitVo placeOrder(Long memberId, PortalSeckillOrderBo bo) {
        portalSeckillRateLimitService.check(memberId);
        SeckillContext ctx = portalSeckillSessionSupport.resolve(bo.getSessionId(), bo.getSkuId());
        if (!"ongoing".equals(ctx.sessionState())) {
            throw new BizException("当前场次未开始或已结束");
        }

        SmsSeckillSkuRelation relation = ctx.relation();
        int quantity = bo.getQuantity();
        int seckillLimit = relation.getSeckillLimit() != null ? relation.getSeckillLimit() : 0;
        if (seckillLimit > 0 && quantity > seckillLimit) {
            throw new BizException("超过单场限购数量：" + seckillLimit);
        }

        Long promotionId = ctx.promotion().getId();
        Long sessionId = bo.getSessionId();
        Long skuId = bo.getSkuId();

        portalSeckillStockService.reserve(promotionId, sessionId, skuId, memberId, quantity, seckillLimit);
        try {
            PortalOrderSubmitBo submitBo = new PortalOrderSubmitBo();
            submitBo.setAddressId(bo.getAddressId());
            submitBo.setUseCart(false);
            submitBo.setOrderToken(bo.getOrderToken());
            submitBo.setPayType(bo.getPayType());
            submitBo.setNote(bo.getNote());

            PortalOrderItemBo item = new PortalOrderItemBo();
            item.setSkuId(skuId);
            item.setQuantity(quantity);
            submitBo.setItems(List.of(item));

            PortalOrderSubmitVo result = portalOrderService.submit(memberId, submitBo);
            portalSeckillStockService.bindOrder(
                    result.getOrderSn(),
                    promotionId,
                    sessionId,
                    skuId,
                    memberId,
                    quantity,
                    seckillLimit);
            return result;
        } catch (RuntimeException ex) {
            portalSeckillStockService.release(promotionId, sessionId, skuId, memberId, quantity, seckillLimit);
            throw ex;
        }
    }
}
