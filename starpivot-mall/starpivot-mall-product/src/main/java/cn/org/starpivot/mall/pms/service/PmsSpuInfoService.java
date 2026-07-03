package cn.org.starpivot.mall.pms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.pms.domain.bo.ProductReqBo;
import cn.org.starpivot.mall.pms.domain.bo.ProductSaveBo;
import cn.org.starpivot.mall.pms.domain.vo.ProductVo;

import java.util.List;

/**
 * Spuinfoservice服务接口。
 * <p>
 * 封装SPU相关业务逻辑。
 * </p>
 */

public interface PmsSpuInfoService {

    /**
     * 获取PmsSpuInfoPageList。
     */
    PageResponse<ProductVo> getPmsSpuInfoPageList(ProductReqBo productReqBo);

    /**
     * 获取PmsSpuInfoById。
     */
    ProductVo getPmsSpuInfoById(Long id);

    /**
     * 新增记录。
     */
    void addPmsSpuInfo(ProductSaveBo bo);

    /**
     * 修改记录。
     */
    void updatePmsSpuInfo(ProductSaveBo bo);

    /**
     * 删除记录。
     */
    void removePmsSpuInfoByIds(List<Long> ids);

    /** 仅更新上架状态（0-下架 1-上架） */
    /**
     * 修改记录。
     */
    void updatePublishStatus(Long id, Integer publishStatus);
}
