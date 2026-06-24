package cn.org.starpivot.mall.pms.service;

import cn.org.starpivot.mall.pms.domain.bo.CategorySaveBo;
import cn.org.starpivot.mall.pms.domain.bo.CategorySortItemBo;
import cn.org.starpivot.mall.pms.entity.PmsCategory;
import cn.org.starpivot.mall.pms.domain.vo.CategoryTreeVo;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface PmsCategoryService extends IService<PmsCategory> {

    List<CategoryTreeVo> treeList();

    List<CategoryTreeVo> listChildren(Long parentCid);

    CategoryTreeVo getDetail(Long catId);

    void addCategory(CategorySaveBo bo);

    void updateCategory(CategorySaveBo bo);

    void removeCategories(List<Long> ids);

    void updateSortBatch(List<CategorySortItemBo> items);
}
