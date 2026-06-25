package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.mall.portal.domain.bo.PortalOrderReturnApplyBo;
import java.util.List;

/**
 * C 端退货申请。
 */
public interface PortalOrderReturnService {

    List<Long> applyReturn(Long memberId, PortalOrderReturnApplyBo bo);
}
