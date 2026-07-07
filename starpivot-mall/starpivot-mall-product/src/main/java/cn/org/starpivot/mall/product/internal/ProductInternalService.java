package cn.org.starpivot.mall.product.internal;

import cn.org.starpivot.api.product.dto.*;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.mall.common.MallProductConstants;
import cn.org.starpivot.mall.pms.domain.vo.CategoryTreeVo;
import cn.org.starpivot.mall.pms.entity.*;
import cn.org.starpivot.mall.pms.mapper.*;
import cn.org.starpivot.mall.pms.service.PmsCategoryService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductInternalService {

    private final PmsSkuInfoMapper pmsSkuInfoMapper;
    private final PmsSpuInfoMapper pmsSpuInfoMapper;
    private final PmsBrandMapper pmsBrandMapper;
    private final PmsCategoryMapper pmsCategoryMapper;
    private final PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    private final PmsCategoryBrandRelationMapper pmsCategoryBrandRelationMapper;
    private final PmsSpuImagesMapper pmsSpuImagesMapper;
    private final PmsSpuCommentMapper pmsSpuCommentMapper;
    private final PmsCategoryService pmsCategoryService;

    public SkuDto getSku(Long skuId) {
        PmsSkuInfo sku = pmsSkuInfoMapper.selectById(skuId);
        if (sku == null) {
            return null;
        }
        return toSkuDto(sku);
    }

    public List<SkuDto> listSkusByIds(List<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return List.of();
        }
        return pmsSkuInfoMapper.selectBatchIds(skuIds).stream().map(this::toSkuDto).toList();
    }

    /** 批量加载 SKU 销售规格文案（展示用，不校验上下架） */
    public Map<Long, String> loadSkuAttrTexts(List<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Map.of();
        }
        return loadSkuAttrMap(skuIds);
    }

    public List<SkuDto> listSkusBySpuIds(List<Long> spuIds) {
        if (CollectionUtils.isEmpty(spuIds)) {
            return List.of();
        }
        return pmsSkuInfoMapper.selectList(
                        Wrappers.<PmsSkuInfo>lambdaQuery().in(PmsSkuInfo::getSpuId, spuIds))
                .stream()
                .map(this::toSkuDto)
                .toList();
    }

    public Map<Long, SkuOrderSnapshotDto> loadOrderSnapshots(List<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Map.of();
        }
        List<PmsSkuInfo> skus = pmsSkuInfoMapper.selectBatchIds(skuIds);
        Map<Long, PmsSkuInfo> skuMap = skus.stream()
                .filter(sku -> sku.getSkuId() != null)
                .collect(Collectors.toMap(PmsSkuInfo::getSkuId, sku -> sku, (a, b) -> a, LinkedHashMap::new));

        List<Long> spuIds = skus.stream()
                .map(PmsSkuInfo::getSpuId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, PmsSpuInfo> spuMap = spuIds.isEmpty()
                ? Map.of()
                : pmsSpuInfoMapper.selectBatchIds(spuIds).stream()
                        .filter(spu -> spu.getId() != null)
                        .collect(Collectors.toMap(PmsSpuInfo::getId, spu -> spu, (a, b) -> a));

        List<Long> brandIds = spuMap.values().stream()
                .map(PmsSpuInfo::getBrandId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> brandNameMap = brandIds.isEmpty()
                ? Map.of()
                : pmsBrandMapper.selectBatchIds(brandIds).stream()
                        .filter(brand -> brand.getBrandId() != null)
                        .collect(Collectors.toMap(PmsBrand::getBrandId, PmsBrand::getName, (a, b) -> a));

        Map<Long, String> attrMap = loadSkuAttrMap(skuIds);

        Map<Long, SkuOrderSnapshotDto> result = new LinkedHashMap<>();
        for (Long skuId : skuIds) {
            PmsSkuInfo sku = skuMap.get(skuId);
            if (sku == null) {
                throw new BizException("SKU不存在：" + skuId);
            }
            PmsSpuInfo spu = sku.getSpuId() == null ? null : spuMap.get(sku.getSpuId());
            if (spu == null || !Integer.valueOf(MallProductConstants.PUBLISH_STATUS_ON).equals(spu.getPublishStatus())) {
                throw new BizException("商品已下架：" + skuId);
            }
            if (sku.getPrice() == null) {
                throw new BizException("商品价格异常：" + skuId);
            }
            SkuOrderSnapshotDto dto = new SkuOrderSnapshotDto();
            dto.setSkuId(sku.getSkuId());
            dto.setSpuId(sku.getSpuId());
            dto.setSkuName(sku.getSkuName());
            dto.setSkuTitle(sku.getSkuTitle());
            dto.setSkuDefaultImg(sku.getSkuDefaultImg());
            dto.setPrice(sku.getPrice());
            dto.setSpuName(spu.getSpuName());
            dto.setCatalogId(spu.getCatalogId());
            dto.setBrandId(spu.getBrandId());
            dto.setPublishStatus(spu.getPublishStatus());
            dto.setBrandName(spu.getBrandId() == null ? null : brandNameMap.get(spu.getBrandId()));
            dto.setAttrs(attrMap.getOrDefault(skuId, ""));
            result.put(skuId, dto);
        }
        return result;
    }

    public SpuDto getSpu(Long spuId) {
        PmsSpuInfo spu = pmsSpuInfoMapper.selectById(spuId);
        if (spu == null) {
            return null;
        }
        return toSpuDto(spu);
    }

    public List<SpuDto> listSpusByIds(List<Long> spuIds) {
        if (CollectionUtils.isEmpty(spuIds)) {
            return List.of();
        }
        return pmsSpuInfoMapper.selectBatchIds(spuIds).stream().map(this::toSpuDto).toList();
    }

    public CategoryDto getCategory(Long categoryId) {
        PmsCategory category = pmsCategoryMapper.selectById(categoryId);
        if (category == null) {
            return null;
        }
        CategoryDto dto = new CategoryDto();
        BeanUtils.copyProperties(category, dto);
        return dto;
    }

    public List<CategoryDto> listCategoriesByIds(List<Long> categoryIds) {
        if (CollectionUtils.isEmpty(categoryIds)) {
            return List.of();
        }
        return pmsCategoryMapper.selectBatchIds(categoryIds).stream().map(category -> {
            CategoryDto dto = new CategoryDto();
            BeanUtils.copyProperties(category, dto);
            return dto;
        }).toList();
    }

    public List<CategoryTreeDto> categoryTree() {
        return pmsCategoryService.treeList().stream().map(this::toCategoryTreeDto).toList();
    }

    public List<BrandDto> listBrandsByIds(List<Long> brandIds) {
        if (CollectionUtils.isEmpty(brandIds)) {
            return List.of();
        }
        return pmsBrandMapper.selectBatchIds(brandIds).stream().map(this::toBrandDto).toList();
    }

    public List<CategoryBrandRelationDto> listCategoryBrands(List<Long> catalogIds) {
        if (CollectionUtils.isEmpty(catalogIds)) {
            return List.of();
        }
        return pmsCategoryBrandRelationMapper.selectList(
                        Wrappers.<PmsCategoryBrandRelation>lambdaQuery()
                                .in(PmsCategoryBrandRelation::getCatelogId, catalogIds))
                .stream()
                .map(this::toCategoryBrandDto)
                .toList();
    }

    public Map<Long, String> spuCoverImages(List<Long> spuIds) {
        if (CollectionUtils.isEmpty(spuIds)) {
            return Map.of();
        }
        List<PmsSpuImages> imgs = pmsSpuImagesMapper.selectList(
                Wrappers.<PmsSpuImages>lambdaQuery()
                        .in(PmsSpuImages::getSpuId, spuIds)
                        .orderByDesc(PmsSpuImages::getDefaultImg)
                        .orderByAsc(PmsSpuImages::getImgSort)
                        .orderByAsc(PmsSpuImages::getId));
        Map<Long, String> coverMap = new LinkedHashMap<>();
        for (PmsSpuImages img : imgs) {
            if (img.getSpuId() == null || !StringUtils.hasText(img.getImgUrl())) {
                continue;
            }
            coverMap.putIfAbsent(img.getSpuId(), normalizeImg(img.getImgUrl()));
        }
        return coverMap;
    }

    public List<SpuDto> listNewestPublishedSpus(int limit) {
        List<PmsSpuInfo> spus = pmsSpuInfoMapper.selectList(
                Wrappers.<PmsSpuInfo>lambdaQuery()
                        .eq(PmsSpuInfo::getPublishStatus, MallProductConstants.PUBLISH_STATUS_ON)
                        .orderByDesc(PmsSpuInfo::getCreateTime)
                        .orderByDesc(PmsSpuInfo::getId)
                        .last("LIMIT " + Math.max(limit, 1)));
        return spus.stream().map(this::toSpuDto).toList();
    }

    public List<SkuDto> listBudgetSkus(int limit) {
        List<PmsSkuInfo> skus = pmsSkuInfoMapper.selectList(
                Wrappers.<PmsSkuInfo>lambdaQuery()
                        .isNotNull(PmsSkuInfo::getPrice)
                        .orderByAsc(PmsSkuInfo::getPrice)
                        .orderByAsc(PmsSkuInfo::getSkuId)
                        .last("LIMIT 40"));
        if (CollectionUtils.isEmpty(skus)) {
            return List.of();
        }
        Set<Long> spuIds = new HashSet<>();
        List<PmsSkuInfo> picked = new ArrayList<>();
        for (PmsSkuInfo sku : skus) {
            if (sku.getSpuId() == null || !spuIds.add(sku.getSpuId())) {
                continue;
            }
            picked.add(sku);
            if (picked.size() >= limit) {
                break;
            }
        }
        Set<Long> publishedSpuIds = pmsSpuInfoMapper.selectBatchIds(spuIds).stream()
                .filter(spu -> Integer.valueOf(MallProductConstants.PUBLISH_STATUS_ON).equals(spu.getPublishStatus()))
                .map(PmsSpuInfo::getId)
                .collect(Collectors.toSet());
        return picked.stream()
                .filter(sku -> sku.getSpuId() != null && publishedSpuIds.contains(sku.getSpuId()))
                .limit(limit)
                .map(this::toSkuDto)
                .toList();
    }

    public void incrementSaleCount(Long skuId, int quantity) {
        if (skuId == null || quantity <= 0) {
            throw new BizException("销量更新参数无效");
        }
        pmsSkuInfoMapper.incrementSaleCount(skuId, quantity);
    }

    public void decrementSaleCount(Long skuId, int quantity) {
        if (skuId == null || quantity <= 0) {
            throw new BizException("销量更新参数无效");
        }
        pmsSkuInfoMapper.decrementSaleCount(skuId, quantity);
    }

    private CategoryTreeDto toCategoryTreeDto(CategoryTreeVo vo) {
        CategoryTreeDto dto = new CategoryTreeDto();
        BeanUtils.copyProperties(vo, dto);
        if (vo.getChildren() != null) {
            dto.setChildren(vo.getChildren().stream().map(this::toCategoryTreeDto).toList());
        }
        return dto;
    }

    private BrandDto toBrandDto(PmsBrand brand) {
        BrandDto dto = new BrandDto();
        dto.setBrandId(brand.getBrandId());
        dto.setName(brand.getName());
        dto.setLogo(brand.getLogo());
        dto.setSort(brand.getSort());
        dto.setShowStatus(brand.getShowStatus());
        return dto;
    }

    private CategoryBrandRelationDto toCategoryBrandDto(PmsCategoryBrandRelation relation) {
        CategoryBrandRelationDto dto = new CategoryBrandRelationDto();
        dto.setCatelogId(relation.getCatelogId());
        dto.setBrandId(relation.getBrandId());
        return dto;
    }

    private String normalizeImg(String raw) {
        return StorageObjectPathUtils.normalizeToObjectName(raw.trim());
    }

    private SkuDto toSkuDto(PmsSkuInfo sku) {
        SkuDto dto = new SkuDto();
        dto.setSkuId(sku.getSkuId());
        dto.setSpuId(sku.getSpuId());
        dto.setSkuName(sku.getSkuName());
        dto.setSkuTitle(sku.getSkuTitle());
        dto.setSkuSubtitle(sku.getSkuSubtitle());
        dto.setSkuDefaultImg(sku.getSkuDefaultImg());
        dto.setCatalogId(sku.getCatalogId());
        dto.setBrandId(sku.getBrandId());
        dto.setPrice(sku.getPrice());
        dto.setSaleCount(sku.getSaleCount());
        dto.setStockWarning(sku.getStockWarning());
        return dto;
    }

    private SpuDto toSpuDto(PmsSpuInfo spu) {
        SpuDto dto = new SpuDto();
        dto.setId(spu.getId());
        dto.setSpuName(spu.getSpuName());
        dto.setCatalogId(spu.getCatalogId());
        dto.setBrandId(spu.getBrandId());
        dto.setPublishStatus(spu.getPublishStatus());
        dto.setAuditStatus(spu.getAuditStatus());
        dto.setWeight(spu.getWeight());
        return dto;
    }

    private Map<Long, String> loadSkuAttrMap(List<Long> skuIds) {
        List<PmsSkuSaleAttrValue> attrs = pmsSkuSaleAttrValueMapper.selectList(
                Wrappers.<PmsSkuSaleAttrValue>lambdaQuery()
                        .in(PmsSkuSaleAttrValue::getSkuId, skuIds)
                        .orderByAsc(PmsSkuSaleAttrValue::getAttrSort));
        Map<Long, StringBuilder> builderMap = new LinkedHashMap<>();
        for (PmsSkuSaleAttrValue attr : attrs) {
            if (attr.getSkuId() == null) {
                continue;
            }
            StringBuilder sb = builderMap.computeIfAbsent(attr.getSkuId(), k -> new StringBuilder());
            if (!sb.isEmpty()) {
                sb.append(';');
            }
            sb.append(attr.getAttrName()).append(':').append(attr.getAttrValue());
        }
        Map<Long, String> result = new LinkedHashMap<>();
        builderMap.forEach((skuId, sb) -> result.put(skuId, sb.toString()));
        return result;
    }

    public int countCommentsByMember(Long memberId) {
        if (memberId == null) {
            return 0;
        }
        Long count = pmsSpuCommentMapper.selectCount(
                Wrappers.<PmsSpuComment>lambdaQuery().eq(PmsSpuComment::getMemberId, memberId));
        return count != null ? count.intValue() : 0;
    }
}
