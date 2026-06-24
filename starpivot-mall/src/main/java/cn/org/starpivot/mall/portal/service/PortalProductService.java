package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.portal.domain.bo.PortalProductSearchBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalProductDetailVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalProductListVo;

public interface PortalProductService {

    PageResponse<PortalProductListVo> search(PortalProductSearchBo bo);

    PortalProductDetailVo getDetail(Long spuId);
}
