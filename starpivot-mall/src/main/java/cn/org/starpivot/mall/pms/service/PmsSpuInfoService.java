package cn.org.starpivot.mall.pms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.pms.domain.bo.ProductReqBo;
import cn.org.starpivot.mall.pms.domain.bo.ProductSaveBo;
import cn.org.starpivot.mall.pms.domain.vo.ProductVo;

import java.util.List;

public interface PmsSpuInfoService {

    PageResponse<ProductVo> getPmsSpuInfoPageList(ProductReqBo productReqBo);

    ProductVo getPmsSpuInfoById(Long id);

    void addPmsSpuInfo(ProductSaveBo bo);

    void updatePmsSpuInfo(ProductSaveBo bo);

    void removePmsSpuInfoByIds(List<Long> ids);

    /** 仅更新上架状态（0-下架 1-上架） */
    void updatePublishStatus(Long id, Integer publishStatus);
}
