package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.api.product.dto.BrandDto;
import cn.org.starpivot.api.product.dto.CategoryBrandRelationDto;
import cn.org.starpivot.api.product.dto.CategoryTreeDto;
import cn.org.starpivot.mall.common.ProductFeignSupport;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.domain.vo.PortalBrandBriefVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalHomeVo;
import cn.org.starpivot.mall.portal.service.PortalHomeService;
import cn.org.starpivot.mall.portal.service.support.PortalHomeMarketingLoader;
import cn.org.starpivot.mall.sms.domain.vo.HomeAdvVo;
import cn.org.starpivot.mall.sms.entity.SmsHomeAdv;
import cn.org.starpivot.mall.sms.mapper.SmsHomeAdvMapper;
import cn.org.starpivot.mall.sms.service.SmsHomeCategoryHotService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Home服务实现类。
 * <p>
 * 实现 {@link PortalHomeService}，处理Home相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see PortalHomeService
 */

@Service
@RequiredArgsConstructor
public class PortalHomeServiceImpl implements PortalHomeService {

    private static final int MAX_BRANDS_PER_CATEGORY = 8;

    private final SmsHomeAdvMapper smsHomeAdvMapper;
    private final ProductFeignSupport productFeignSupport;
    private final PortalHomeMarketingLoader portalHomeMarketingLoader;
    private final SmsHomeCategoryHotService smsHomeCategoryHotService;

    @Override
    @Transactional(readOnly = true)
    public PortalHomeVo getHomeData() {
        PortalHomeVo vo = new PortalHomeVo();
        vo.setBanners(listActiveBanners());
        List<CategoryTreeDto> categories = filterVisibleCategories(productFeignSupport.requireCategoryTree());
        vo.setCategories(categories);
        vo.setCategoryBrands(buildCategoryBrands(categories));
        vo.setHotCategories(smsHomeCategoryHotService.listActive());
        vo.setHomeBlocks(portalHomeMarketingLoader.loadHomeBlocks());
        return vo;
    }

    private Map<Long, List<PortalBrandBriefVo>> buildCategoryBrands(List<CategoryTreeDto> l1Categories) {
        if (CollectionUtils.isEmpty(l1Categories)) {
            return Collections.emptyMap();
        }
        Map<Long, Set<Long>> l1ToLeafIds = new LinkedHashMap<>();
        Map<Long, Long> leafToL1 = new HashMap<>();
        for (CategoryTreeDto l1 : l1Categories) {
            if (l1.getCatId() == null) {
                continue;
            }
            Set<Long> leafIds = new HashSet<>();
            collectLeafCategoryIds(l1, leafIds);
            if (!leafIds.isEmpty()) {
                l1ToLeafIds.put(l1.getCatId(), leafIds);
                for (Long leafId : leafIds) {
                    leafToL1.put(leafId, l1.getCatId());
                }
            }
        }
        if (leafToL1.isEmpty()) {
            return Collections.emptyMap();
        }
        List<CategoryBrandRelationDto> relations = productFeignSupport.requireCategoryBrands(
                new ArrayList<>(leafToL1.keySet()));
        if (CollectionUtils.isEmpty(relations)) {
            return Collections.emptyMap();
        }
        Map<Long, Set<Long>> l1ToBrandIds = new LinkedHashMap<>();
        for (CategoryBrandRelationDto relation : relations) {
            if (relation.getBrandId() == null || relation.getCatelogId() == null) {
                continue;
            }
            Long l1Id = leafToL1.get(relation.getCatelogId());
            if (l1Id == null) {
                continue;
            }
            l1ToBrandIds.computeIfAbsent(l1Id, k -> new LinkedHashSet<>()).add(relation.getBrandId());
        }
        Set<Long> allBrandIds = l1ToBrandIds.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        if (allBrandIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, BrandDto> brandMap = productFeignSupport.requireBrandMap(new ArrayList<>(allBrandIds)).entrySet()
                .stream()
                .filter(e -> isBrandVisible(e.getValue().getShowStatus()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));
        Map<Long, List<PortalBrandBriefVo>> result = new LinkedHashMap<>();
        for (Map.Entry<Long, Set<Long>> entry : l1ToBrandIds.entrySet()) {
            List<PortalBrandBriefVo> brands = entry.getValue().stream()
                    .map(brandMap::get)
                    .filter(brand -> brand != null)
                    .sorted((a, b) -> {
                        int sortA = a.getSort() != null ? a.getSort() : 0;
                        int sortB = b.getSort() != null ? b.getSort() : 0;
                        return Integer.compare(sortA, sortB);
                    })
                    .limit(MAX_BRANDS_PER_CATEGORY)
                    .map(this::toBrandBrief)
                    .collect(Collectors.toList());
            if (!brands.isEmpty()) {
                result.put(entry.getKey(), brands);
            }
        }
        return result;
    }

    private void collectLeafCategoryIds(CategoryTreeDto node, Set<Long> leafIds) {
        if (node == null || node.getCatId() == null) {
            return;
        }
        List<CategoryTreeDto> children = node.getChildren();
        if (CollectionUtils.isEmpty(children)) {
            leafIds.add(node.getCatId());
            return;
        }
        for (CategoryTreeDto child : children) {
            collectLeafCategoryIds(child, leafIds);
        }
    }

    private PortalBrandBriefVo toBrandBrief(BrandDto brand) {
        PortalBrandBriefVo vo = new PortalBrandBriefVo();
        vo.setBrandId(brand.getBrandId());
        vo.setName(brand.getName());
        vo.setLogo(brand.getLogo());
        return vo;
    }

    private boolean isBrandVisible(Integer showStatus) {
        return showStatus == null || showStatus == 1;
    }

    private List<HomeAdvVo> listActiveBanners() {
        LocalDateTime now = LocalDateTime.now();
        List<SmsHomeAdv> list = smsHomeAdvMapper.selectList(
                Wrappers.<SmsHomeAdv>lambdaQuery()
                        .eq(SmsHomeAdv::getStatus, PortalConstants.ADV_STATUS_ON)
                        .and(w -> w.isNull(SmsHomeAdv::getStartTime).or().le(SmsHomeAdv::getStartTime, now))
                        .and(w -> w.isNull(SmsHomeAdv::getEndTime).or().ge(SmsHomeAdv::getEndTime, now))
                        .orderByAsc(SmsHomeAdv::getSort)
                        .orderByDesc(SmsHomeAdv::getId));
        return list.stream().map(this::toAdvVo).collect(Collectors.toList());
    }

    private HomeAdvVo toAdvVo(SmsHomeAdv entity) {
        HomeAdvVo vo = new HomeAdvVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private List<CategoryTreeDto> filterVisibleCategories(List<CategoryTreeDto> tree) {
        if (tree == null) {
            return List.of();
        }
        List<CategoryTreeDto> result = new ArrayList<>();
        for (CategoryTreeDto node : tree) {
            CategoryTreeDto filtered = filterNode(node);
            if (filtered != null) {
                result.add(filtered);
            }
        }
        return result;
    }

    private CategoryTreeDto filterNode(CategoryTreeDto node) {
        if (node == null || !isCategoryVisible(node.getShowStatus())) {
            return null;
        }
        CategoryTreeDto copy = new CategoryTreeDto();
        BeanUtils.copyProperties(node, copy);
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            List<CategoryTreeDto> children = node.getChildren().stream()
                    .map(this::filterNode)
                    .filter(c -> c != null)
                    .collect(Collectors.toList());
            copy.setChildren(children);
        }
        return copy;
    }

    private boolean isCategoryVisible(Long showStatus) {
        return showStatus == null || showStatus == 1L;
    }
}
