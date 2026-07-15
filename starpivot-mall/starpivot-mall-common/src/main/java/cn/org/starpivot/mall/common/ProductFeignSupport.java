package cn.org.starpivot.mall.common;

import cn.org.starpivot.api.product.ProductInternalClient;
import cn.org.starpivot.api.product.dto.*;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductFeignSupport {

    private static final String ACTION = "商品服务";

    private final ProductInternalClient productInternalClient;

    public SkuDto requireSku(Long skuId) {
        return unwrap(productInternalClient.getSku(skuId), "SKU不存在");
    }

    public List<SkuDto> requireSkus(List<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return List.of();
        }
        return unwrap(productInternalClient.listSkusByIds(skuIds), "SKU查询失败");
    }

    public List<SkuDto> requireSkusBySpuIds(List<Long> spuIds) {
        if (CollectionUtils.isEmpty(spuIds)) {
            return List.of();
        }
        return unwrap(productInternalClient.listSkusBySpuIds(spuIds), "SKU查询失败");
    }

    public Map<Long, SkuDto> requireSkuMap(List<Long> skuIds) {
        return requireSkus(skuIds).stream()
                .filter(sku -> sku.getSkuId() != null)
                .collect(Collectors.toMap(SkuDto::getSkuId, sku -> sku, (a, b) -> a, LinkedHashMap::new));
    }

    public Map<Long, SkuOrderSnapshotDto> requireOrderSnapshots(List<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Map.of();
        }
        return unwrap(productInternalClient.loadOrderSnapshots(skuIds), "SKU快照加载失败");
    }

    public Map<Long, String> loadSkuAttrTexts(List<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Map.of();
        }
        Map<Long, String> data = unwrap(productInternalClient.loadSkuAttrTexts(skuIds), "SKU规格加载失败");
        return data == null ? Map.of() : data;
    }

    public SpuDto requireSpu(Long spuId) {
        return unwrap(productInternalClient.getSpu(spuId), "SPU不存在");
    }

    public List<SpuDto> requireSpus(List<Long> spuIds) {
        if (CollectionUtils.isEmpty(spuIds)) {
            return List.of();
        }
        return unwrap(productInternalClient.listSpusByIds(spuIds), "SPU查询失败");
    }

    public Map<Long, SpuDto> requireSpuMap(List<Long> spuIds) {
        return requireSpus(spuIds).stream()
                .filter(spu -> spu.getId() != null)
                .collect(Collectors.toMap(SpuDto::getId, spu -> spu, (a, b) -> a, LinkedHashMap::new));
    }

    public CategoryDto requireCategory(Long categoryId) {
        return unwrap(productInternalClient.getCategory(categoryId), "分类不存在");
    }

    public List<CategoryDto> requireCategories(List<Long> categoryIds) {
        if (CollectionUtils.isEmpty(categoryIds)) {
            return List.of();
        }
        return unwrap(productInternalClient.listCategoriesByIds(categoryIds), "分类查询失败");
    }

    public Map<Long, CategoryDto> requireCategoryMap(List<Long> categoryIds) {
        return requireCategories(categoryIds).stream()
                .filter(category -> category.getCatId() != null)
                .collect(Collectors.toMap(CategoryDto::getCatId, category -> category, (a, b) -> a, LinkedHashMap::new));
    }

    public List<CategoryTreeDto> requireCategoryTree() {
        return unwrap(productInternalClient.categoryTree(), "分类树加载失败");
    }

    public List<BrandDto> requireBrands(List<Long> brandIds) {
        if (CollectionUtils.isEmpty(brandIds)) {
            return List.of();
        }
        return unwrap(productInternalClient.listBrandsByIds(brandIds), "品牌查询失败");
    }

    public Map<Long, BrandDto> requireBrandMap(List<Long> brandIds) {
        return requireBrands(brandIds).stream()
                .filter(brand -> brand.getBrandId() != null)
                .collect(Collectors.toMap(BrandDto::getBrandId, brand -> brand, (a, b) -> a, LinkedHashMap::new));
    }

    public List<CategoryBrandRelationDto> requireCategoryBrands(List<Long> catalogIds) {
        if (CollectionUtils.isEmpty(catalogIds)) {
            return List.of();
        }
        return unwrap(productInternalClient.listCategoryBrands(catalogIds), "分类品牌关联查询失败");
    }

    public Map<Long, String> requireSpuCoverImages(List<Long> spuIds) {
        if (CollectionUtils.isEmpty(spuIds)) {
            return Map.of();
        }
        Map<Long, String> data = unwrap(productInternalClient.spuCoverImages(spuIds), "封面图加载失败");
        return data == null ? Map.of() : data;
    }

    public List<SpuDto> requireNewestPublishedSpus(int limit) {
        return unwrap(productInternalClient.listNewestPublishedSpus(limit), "新品加载失败");
    }

    public List<SkuDto> requireBudgetSkus(int limit) {
        return unwrap(productInternalClient.listBudgetSkus(limit), "包邮商品加载失败");
    }

    public void incrementSaleCount(Long skuId, int quantity) {
        SkuSaleCountRequest request = new SkuSaleCountRequest();
        request.setSkuId(skuId);
        request.setQuantity(quantity);
        unwrapVoid(productInternalClient.incrementSaleCount(request));
    }

    public void decrementSaleCount(Long skuId, int quantity) {
        SkuSaleCountRequest request = new SkuSaleCountRequest();
        request.setSkuId(skuId);
        request.setQuantity(quantity);
        unwrapVoid(productInternalClient.decrementSaleCount(request));
    }

    public PageResponse<PortalProductListDto> listPortalProductsByOrderedSpuIds(
            List<Long> orderedSpuIds, int pageNum, int pageSize) {
        PortalProductListPageRequest request = new PortalProductListPageRequest();
        request.setOrderedSpuIds(orderedSpuIds);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        return unwrap(productInternalClient.listPortalProductsByOrderedSpuIds(request), "商品列表加载失败");
    }

    public int countPendingReviews(Long memberId) {
        return unwrap(productInternalClient.countPendingReviews(memberId), "待评价数量加载失败");
    }

    public int countCommentsByMember(Long memberId) {
        Integer count = unwrap(productInternalClient.countCommentsByMember(memberId), "评价数量加载失败");
        return count == null ? 0 : count;
    }

    private void unwrapVoid(Result<Void> result) {
        if (result == null || !result.isSuccess()) {
            throw new BizException(result != null ? result.getMessage() : ACTION + "不可用");
        }
    }

    private <T> T unwrap(Result<T> result, String notFoundMessage) {
        if (result == null) {
            throw new BizException(ACTION + "不可用");
        }
        if (!result.isSuccess()) {
            if (result.getCode() == ErrorCode.NOT_FOUND) {
                throw new BizException(notFoundMessage);
            }
            throw new BizException(result.getMessage());
        }
        if (result.getData() == null) {
            throw new BizException(notFoundMessage);
        }
        return result.getData();
    }
}
