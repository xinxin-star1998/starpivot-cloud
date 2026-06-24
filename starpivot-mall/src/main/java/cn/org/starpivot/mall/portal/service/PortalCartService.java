package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.mall.portal.domain.bo.PortalCartAddBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalCartUpdateBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCartVo;
import java.util.List;

public interface PortalCartService {

    PortalCartVo listCart(Long memberId);

    void addItem(Long memberId, PortalCartAddBo bo);

    void updateItem(Long memberId, PortalCartUpdateBo bo);

    void removeItems(Long memberId, List<Long> skuIds);

    void clearChecked(Long memberId, List<Long> skuIds);
}
