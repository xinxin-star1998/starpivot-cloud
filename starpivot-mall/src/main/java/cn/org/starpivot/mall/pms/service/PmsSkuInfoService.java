package cn.org.starpivot.mall.pms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.pms.domain.bo.SkuCreateBo;
import cn.org.starpivot.mall.pms.domain.bo.SkuPriceBo;
import cn.org.starpivot.mall.pms.domain.bo.SkuPublishStatusBo;
import cn.org.starpivot.mall.pms.domain.bo.SkuReqBo;
import cn.org.starpivot.mall.pms.domain.bo.SkuSaveBo;
import cn.org.starpivot.mall.pms.domain.vo.SkuVo;

import java.util.List;

public interface PmsSkuInfoService {

    PageResponse<SkuVo> getSkuPageList(SkuReqBo reqBo);

    SkuVo getSkuById(Long skuId);

    Long createSku(SkuCreateBo bo);

    void updateSku(SkuSaveBo bo);

    void updatePrice(SkuPriceBo bo);

    void updatePublishStatus(SkuPublishStatusBo bo);

    void removeByIds(List<Long> ids);
}
