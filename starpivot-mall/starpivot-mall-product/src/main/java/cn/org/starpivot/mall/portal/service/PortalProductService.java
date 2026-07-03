package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.portal.domain.bo.PortalProductSearchBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalProductDetailVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalProductListVo;

import java.util.List;

/**
 * Productservice服务接口。
 * <p>
 * 封装Product相关业务逻辑。
 * </p>
 */
public interface PortalProductService {

    /**
     * search。
     */
    PageResponse<PortalProductListVo> search(PortalProductSearchBo bo);

    /**
     * 获取Detail。
     */
    PortalProductDetailVo getDetail(Long spuId);

    /** 同分类/同品牌相关推荐 */
    List<PortalProductListVo> listRelated(Long spuId, int limit);

    /** 按指定 SPU 顺序分页（仅上架商品） */
    PageResponse<PortalProductListVo> listByOrderedSpuIds(List<Long> orderedSpuIds, int pageNum, int pageSize);
}
