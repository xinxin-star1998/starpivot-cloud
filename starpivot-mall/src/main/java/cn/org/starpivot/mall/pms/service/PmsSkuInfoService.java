package cn.org.starpivot.mall.pms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.pms.domain.bo.*;
import cn.org.starpivot.mall.pms.domain.vo.SkuVo;

import java.util.List;

/**
 * Skuinfoservice服务接口。
 * <p>
 * 封装SKU相关业务逻辑。
 * </p>
 */

public interface PmsSkuInfoService {

    /**
     * 获取SkuPageList。
     */
    PageResponse<SkuVo> getSkuPageList(SkuReqBo reqBo);

    /**
     * 获取SkuById。
     */
    SkuVo getSkuById(Long skuId);

    /**
     * createSku。
     */
    Long createSku(SkuCreateBo bo);

    /**
     * 修改记录。
     */
    void updateSku(SkuSaveBo bo);

    /**
     * 修改记录。
     */
    void updatePrice(SkuPriceBo bo);

    /**
     * 修改记录。
     */
    void updatePublishStatus(SkuPublishStatusBo bo);

    /**
     * 批量删除。
     */
    void removeByIds(List<Long> ids);
}
