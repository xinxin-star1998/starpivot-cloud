package cn.org.starpivot.mall.pms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.pms.domain.bo.BrandCategoryBindBo;
import cn.org.starpivot.mall.pms.domain.bo.BrandReqBo;
import cn.org.starpivot.mall.pms.domain.bo.BrandSaveBo;
import cn.org.starpivot.mall.pms.domain.vo.BrandCategoryVo;
import cn.org.starpivot.mall.pms.domain.vo.BrandVo;
import cn.org.starpivot.mall.pms.domain.vo.CategoryTreeVo;
import cn.org.starpivot.mall.pms.entity.PmsBrand;
import cn.org.starpivot.mall.pms.entity.PmsCategoryBrandRelation;
import cn.org.starpivot.mall.pms.mapper.PmsBrandMapper;
import cn.org.starpivot.mall.pms.mapper.PmsCategoryBrandRelationMapper;
import cn.org.starpivot.mall.pms.service.BrandService;
import cn.org.starpivot.mall.pms.service.PmsCategoryService;
import cn.org.starpivot.mall.pms.support.MallFileRefSupport;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 品牌服务实现类。
 * <p>
 * 实现 {@link BrandService}，处理品牌相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see BrandService
 */

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final PmsBrandMapper pmsBrandMapper;
    private final PmsCategoryBrandRelationMapper categoryBrandRelationMapper;
    private final PmsCategoryService pmsCategoryService;
    private final MallFileRefSupport mallFileRefSupport;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BrandVo> pageList(BrandReqBo brandReqBo) {
        Page<PmsBrand> page = new Page<>(brandReqBo.getPageNum(), brandReqBo.getPageSize());
        IPage<PmsBrand> pageList = pmsBrandMapper.selectPageList(page, brandReqBo);
        PageResponse<BrandVo> pageResponse = new PageResponse<>();
        pageResponse.setTotal(pageList.getTotal());
        pageResponse.setRows(pageList.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        pageResponse.setPageNum(pageList.getCurrent());
        pageResponse.setPageSize(pageList.getSize());
        pageResponse.setPageCount(pageList.getPages());
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public BrandVo getById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "品牌ID不能为空");
        }
        PmsBrand brand = pmsBrandMapper.selectById(id);
        if (brand == null) {
            throw new BizException("品牌不存在");
        }
        return toVo(brand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBrand(BrandSaveBo bo) {
        PmsBrand entity = new PmsBrand();
        BeanUtils.copyProperties(bo, entity);
        entity.setBrandId(null);
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        pmsBrandMapper.insert(entity);
        mallFileRefSupport.syncBrandLogo(entity.getBrandId(), entity.getLogo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBrand(BrandSaveBo bo) {
        if (bo.getBrandId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "修改品牌时 brandId 不能为空");
        }
        PmsBrand existing = pmsBrandMapper.selectById(bo.getBrandId());
        if (existing == null) {
            throw new BizException("品牌不存在");
        }
        existing.setName(bo.getName());
        existing.setLogo(bo.getLogo());
        existing.setDescript(bo.getDescript());
        if (bo.getSort() != null) {
            existing.setSort(bo.getSort());
        }
        existing.setShowStatus(bo.getShowStatus());
        existing.setFirstLetter(bo.getFirstLetter());
        pmsBrandMapper.updateById(existing);
        mallFileRefSupport.syncBrandLogo(existing.getBrandId(), existing.getLogo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        for (Long brandId : ids) {
            if (brandId != null) {
                mallFileRefSupport.unbindBrand(brandId);
            }
        }
        pmsBrandMapper.delete(Wrappers.<PmsBrand>lambdaQuery().in(PmsBrand::getBrandId, ids));
        categoryBrandRelationMapper.delete(
                Wrappers.<PmsCategoryBrandRelation>lambdaQuery()
                        .in(PmsCategoryBrandRelation::getBrandId, ids));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandCategoryVo> listBoundCategories(Long brandId) {
        if (brandId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "品牌ID不能为空");
        }
        if (pmsBrandMapper.selectById(brandId) == null) {
            throw new BizException("品牌不存在");
        }
        return categoryBrandRelationMapper
                .selectList(
                        Wrappers.<PmsCategoryBrandRelation>lambdaQuery()
                                .eq(PmsCategoryBrandRelation::getBrandId, brandId)
                                .orderByAsc(PmsCategoryBrandRelation::getId))
                .stream()
                .map(this::toCategoryVo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindCategories(BrandCategoryBindBo bo) {
        if (bo.getBrandId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "品牌ID不能为空");
        }
        PmsBrand brand = pmsBrandMapper.selectById(bo.getBrandId());
        if (brand == null) {
            throw new BizException("品牌不存在");
        }
        List<Long> catIds =
                bo.getCatIds() == null
                        ? Collections.emptyList()
                        : bo.getCatIds().stream().filter(Objects::nonNull).distinct().toList();
        Map<Long, CategoryTreeVo> categoryById = loadAndValidateLevel3Categories(catIds);

        categoryBrandRelationMapper.delete(
                Wrappers.<PmsCategoryBrandRelation>lambdaQuery()
                        .eq(PmsCategoryBrandRelation::getBrandId, bo.getBrandId()));
        if (catIds.isEmpty()) {
            return;
        }
        String brandName = brand.getName();
        for (Long catId : catIds) {
            CategoryTreeVo category = categoryById.get(catId);
            PmsCategoryBrandRelation row = new PmsCategoryBrandRelation();
            row.setBrandId(bo.getBrandId());
            row.setCatelogId(catId);
            row.setBrandName(brandName);
            row.setCatelogName(category != null ? category.getName() : null);
            categoryBrandRelationMapper.insert(row);
        }
    }

    private Map<Long, CategoryTreeVo> loadAndValidateLevel3Categories(List<Long> catIds) {
        if (catIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, CategoryTreeVo> byId = new HashMap<>(catIds.size());
        for (Long catId : catIds) {
            CategoryTreeVo vo = pmsCategoryService.getDetail(catId);
            Long lv = vo.getCatLevel();
            if (lv == null || lv != 3L) {
                throw new BizException("仅允许绑定三级分类：" + vo.getName());
            }
            byId.put(catId, vo);
        }
        return byId;
    }

    private BrandCategoryVo toCategoryVo(PmsCategoryBrandRelation row) {
        BrandCategoryVo vo = new BrandCategoryVo();
        vo.setId(row.getId());
        vo.setCatelogId(row.getCatelogId());
        vo.setCatelogName(row.getCatelogName());
        return vo;
    }

    private BrandVo toVo(PmsBrand brand) {
        BrandVo vo = new BrandVo();
        BeanUtils.copyProperties(brand, vo);
        return vo;
    }
}
