package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.mall.portal.domain.bo.PortalSeckillOrderBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderSubmitVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalSeckillPageVo;

/**
 * C 端秒杀页服务。
 */
public interface PortalSeckillService {

    PortalSeckillPageVo getPage(Long sessionId);

    PortalOrderSubmitVo placeOrder(Long memberId, PortalSeckillOrderBo bo);
}
