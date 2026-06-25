package cn.org.starpivot.mall.pms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.pms.domain.bo.BrandCategoryBindBo;
import cn.org.starpivot.mall.pms.domain.bo.BrandReqBo;
import cn.org.starpivot.mall.pms.domain.bo.BrandSaveBo;
import cn.org.starpivot.mall.pms.domain.vo.BrandCategoryVo;
import cn.org.starpivot.mall.pms.domain.vo.BrandVo;

import java.util.List;

/**
 * Brandservice服务接口。
 * <p>
 * 封装品牌相关业务逻辑。
 * </p>
 */

public interface BrandService {

    /**
     * 分页查询列表。
     */
    PageResponse<BrandVo> pageList(BrandReqBo brandReqBo);

    /**
     * 根据 ID 获取详情。
     */
    BrandVo getById(Long id);

    /**
     * 新增记录。
     */
    void addBrand(BrandSaveBo bo);

    /**
     * 修改记录。
     */
    void updateBrand(BrandSaveBo bo);

    /**
     * 批量删除。
     */
    void removeByIds(List<Long> ids);

    /**
     * 查询已绑定分类。
     */
    List<BrandCategoryVo> listBoundCategories(Long brandId);

    /**
     * 绑定分类。
     */
    void bindCategories(BrandCategoryBindBo bo);
}
