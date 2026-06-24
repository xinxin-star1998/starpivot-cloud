package cn.org.starpivot.mall.pms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.pms.domain.bo.BrandCategoryBindBo;
import cn.org.starpivot.mall.pms.domain.bo.BrandReqBo;
import cn.org.starpivot.mall.pms.domain.bo.BrandSaveBo;
import cn.org.starpivot.mall.pms.domain.vo.BrandCategoryVo;
import cn.org.starpivot.mall.pms.domain.vo.BrandVo;
import java.util.List;

public interface BrandService {

    PageResponse<BrandVo> pageList(BrandReqBo brandReqBo);

    BrandVo getById(Long id);

    void addBrand(BrandSaveBo bo);

    void updateBrand(BrandSaveBo bo);

    void removeByIds(List<Long> ids);

    List<BrandCategoryVo> listBoundCategories(Long brandId);

    void bindCategories(BrandCategoryBindBo bo);
}
