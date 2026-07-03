package cn.org.starpivot.mall.pms.service;

import cn.org.starpivot.mall.pms.domain.bo.CategorySaveBo;
import cn.org.starpivot.mall.pms.domain.bo.CategorySortItemBo;
import cn.org.starpivot.mall.pms.domain.vo.CategoryTreeVo;
import cn.org.starpivot.mall.pms.entity.PmsCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * Categoryservice服务接口。
 * <p>
 * 封装商品分类相关业务逻辑。
 * </p>
 */

public interface PmsCategoryService extends IService<PmsCategory> {

    /**
     * treeList。
     */
    List<CategoryTreeVo> treeList();

    /**
     * 查询列表。
     */
    List<CategoryTreeVo> listChildren(Long parentCid);

    /**
     * 获取Detail。
     */
    CategoryTreeVo getDetail(Long catId);

    /**
     * 新增记录。
     */
    void addCategory(CategorySaveBo bo);

    /**
     * 修改记录。
     */
    void updateCategory(CategorySaveBo bo);

    /**
     * 删除记录。
     */
    void removeCategories(List<Long> ids);

    /**
     * 修改记录。
     */
    void updateSortBatch(List<CategorySortItemBo> items);
}
