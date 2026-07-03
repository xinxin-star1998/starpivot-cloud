package cn.org.starpivot.api.product;

import cn.org.starpivot.api.fallback.ProductInternalClientFallbackFactory;
import cn.org.starpivot.api.product.dto.*;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品服务内部 Feign 客户端（供 starpivot-mall 等调用）。
 */
@FeignClient(
        name = "starpivot-mall-product",
        contextId = "productInternalClient",
        path = "/api/${starpivot.api.version:v1}",
        fallbackFactory = ProductInternalClientFallbackFactory.class)
public interface ProductInternalClient {

    @GetMapping("/internal/product/sku/{skuId}")
    Result<SkuDto> getSku(@PathVariable("skuId") Long skuId);

    @PostMapping("/internal/product/sku/batch-ids")
    Result<List<SkuDto>> listSkusByIds(@RequestBody List<Long> skuIds);

    @PostMapping("/internal/product/sku/order-snapshot")
    Result<Map<Long, SkuOrderSnapshotDto>> loadOrderSnapshots(@RequestBody List<Long> skuIds);

    @GetMapping("/internal/product/spu/{spuId}")
    Result<SpuDto> getSpu(@PathVariable("spuId") Long spuId);

    @PostMapping("/internal/product/spu/batch-ids")
    Result<List<SpuDto>> listSpusByIds(@RequestBody List<Long> spuIds);

    @GetMapping("/internal/product/category/{categoryId}")
    Result<CategoryDto> getCategory(@PathVariable("categoryId") Long categoryId);

    @PostMapping("/internal/product/category/batch-ids")
    Result<List<CategoryDto>> listCategoriesByIds(@RequestBody List<Long> categoryIds);

    @GetMapping("/internal/product/category/tree")
    Result<List<CategoryTreeDto>> categoryTree();

    @PostMapping("/internal/product/brand/batch-ids")
    Result<List<BrandDto>> listBrandsByIds(@RequestBody List<Long> brandIds);

    @PostMapping("/internal/product/category-brand/by-catalog-ids")
    Result<List<CategoryBrandRelationDto>> listCategoryBrands(@RequestBody List<Long> catalogIds);

    @PostMapping("/internal/product/spu/cover-images")
    Result<Map<Long, String>> spuCoverImages(@RequestBody List<Long> spuIds);

    @GetMapping("/internal/product/spu/newest")
    Result<List<SpuDto>> listNewestPublishedSpus(@RequestParam(value = "limit", defaultValue = "4") int limit);

    @GetMapping("/internal/product/sku/budget")
    Result<List<SkuDto>> listBudgetSkus(@RequestParam(value = "limit", defaultValue = "4") int limit);

    @PostMapping("/internal/product/sku/increment-sale-count")
    Result<Void> incrementSaleCount(@RequestBody SkuSaleCountRequest request);

    @PostMapping("/internal/product/sku/decrement-sale-count")
    Result<Void> decrementSaleCount(@RequestBody SkuSaleCountRequest request);

    @PostMapping("/internal/product/spu/portal-list-page")
    Result<cn.org.starpivot.common.entity.PageResponse<PortalProductListDto>> listPortalProductsByOrderedSpuIds(
            @RequestBody PortalProductListPageRequest request);

    @GetMapping("/internal/product/comment/pending-review-count/{memberId}")
    Result<Integer> countPendingReviews(@PathVariable("memberId") Long memberId);
}
