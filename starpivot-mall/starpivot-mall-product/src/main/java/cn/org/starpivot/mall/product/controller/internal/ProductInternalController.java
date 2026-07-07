package cn.org.starpivot.mall.product.controller.internal;

import cn.org.starpivot.api.product.dto.*;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.portal.domain.vo.PortalProductListVo;
import cn.org.starpivot.mall.portal.service.PortalCommentService;
import cn.org.starpivot.mall.portal.service.PortalProductService;
import cn.org.starpivot.mall.product.internal.ProductInternalService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Hidden
@RestController
@RequestMapping("/internal/product")
@RequiredArgsConstructor
public class ProductInternalController {

    private final ProductInternalService productInternalService;
    private final PortalProductService portalProductService;
    private final PortalCommentService portalCommentService;

    @GetMapping("/sku/{skuId}")
    public Result<SkuDto> getSku(@PathVariable("skuId") Long skuId) {
        SkuDto sku = productInternalService.getSku(skuId);
        if (sku == null) {
            return Result.notFound("SKU不存在");
        }
        return Result.success(sku);
    }

    @PostMapping("/sku/batch-ids")
    public Result<List<SkuDto>> listSkusByIds(@RequestBody List<Long> skuIds) {
        return Result.success(productInternalService.listSkusByIds(skuIds));
    }

    @PostMapping("/sku/by-spu-ids")
    public Result<List<SkuDto>> listSkusBySpuIds(@RequestBody List<Long> spuIds) {
        return Result.success(productInternalService.listSkusBySpuIds(spuIds));
    }

    @PostMapping("/sku/order-snapshot")
    public Result<Map<Long, SkuOrderSnapshotDto>> loadOrderSnapshots(@RequestBody List<Long> skuIds) {
        return Result.success(productInternalService.loadOrderSnapshots(skuIds));
    }

    @PostMapping("/sku/sale-attrs")
    public Result<Map<Long, String>> loadSkuAttrTexts(@RequestBody List<Long> skuIds) {
        return Result.success(productInternalService.loadSkuAttrTexts(skuIds));
    }

    @GetMapping("/spu/{spuId}")
    public Result<SpuDto> getSpu(@PathVariable("spuId") Long spuId) {
        SpuDto spu = productInternalService.getSpu(spuId);
        if (spu == null) {
            return Result.notFound("SPU不存在");
        }
        return Result.success(spu);
    }

    @PostMapping("/spu/batch-ids")
    public Result<List<SpuDto>> listSpusByIds(@RequestBody List<Long> spuIds) {
        return Result.success(productInternalService.listSpusByIds(spuIds));
    }

    @GetMapping("/category/{categoryId}")
    public Result<CategoryDto> getCategory(@PathVariable("categoryId") Long categoryId) {
        CategoryDto category = productInternalService.getCategory(categoryId);
        if (category == null) {
            return Result.notFound("分类不存在");
        }
        return Result.success(category);
    }

    @PostMapping("/category/batch-ids")
    public Result<List<CategoryDto>> listCategoriesByIds(@RequestBody List<Long> categoryIds) {
        return Result.success(productInternalService.listCategoriesByIds(categoryIds));
    }

    @GetMapping("/category/tree")
    public Result<List<CategoryTreeDto>> categoryTree() {
        return Result.success(productInternalService.categoryTree());
    }

    @PostMapping("/brand/batch-ids")
    public Result<List<BrandDto>> listBrandsByIds(@RequestBody List<Long> brandIds) {
        return Result.success(productInternalService.listBrandsByIds(brandIds));
    }

    @PostMapping("/category-brand/by-catalog-ids")
    public Result<List<CategoryBrandRelationDto>> listCategoryBrands(@RequestBody List<Long> catalogIds) {
        return Result.success(productInternalService.listCategoryBrands(catalogIds));
    }

    @PostMapping("/spu/cover-images")
    public Result<Map<Long, String>> spuCoverImages(@RequestBody List<Long> spuIds) {
        return Result.success(productInternalService.spuCoverImages(spuIds));
    }

    @GetMapping("/spu/newest")
    public Result<List<SpuDto>> listNewestPublishedSpus(@RequestParam(value = "limit", defaultValue = "4") int limit) {
        return Result.success(productInternalService.listNewestPublishedSpus(limit));
    }

    @GetMapping("/sku/budget")
    public Result<List<SkuDto>> listBudgetSkus(@RequestParam(value = "limit", defaultValue = "4") int limit) {
        return Result.success(productInternalService.listBudgetSkus(limit));
    }

    @PostMapping("/sku/increment-sale-count")
    public Result<Void> incrementSaleCount(@RequestBody SkuSaleCountRequest request) {
        productInternalService.incrementSaleCount(request.getSkuId(), request.getQuantity());
        return Result.success();
    }

    @PostMapping("/sku/decrement-sale-count")
    public Result<Void> decrementSaleCount(@RequestBody SkuSaleCountRequest request) {
        productInternalService.decrementSaleCount(request.getSkuId(), request.getQuantity());
        return Result.success();
    }

    @PostMapping("/spu/portal-list-page")
    public Result<PageResponse<PortalProductListDto>> listPortalProductsByOrderedSpuIds(
            @RequestBody PortalProductListPageRequest request) {
        PageResponse<PortalProductListVo> page = portalProductService.listByOrderedSpuIds(
                request.getOrderedSpuIds(), request.getPageNum(), request.getPageSize());
        PageResponse<PortalProductListDto> result = new PageResponse<>();
        result.setTotal(page.getTotal());
        result.setPageNum(page.getPageNum());
        result.setPageSize(page.getPageSize());
        result.setPageCount(page.getPageCount());
        result.setRows(page.getRows() == null ? List.of() : page.getRows().stream().map(this::toPortalProductListDto).toList());
        return Result.success(result);
    }

    @GetMapping("/comment/pending-review-count/{memberId}")
    public Result<Integer> countPendingReviews(@PathVariable("memberId") Long memberId) {
        return Result.success(portalCommentService.countPendingReviews(memberId));
    }

    @GetMapping("/comment/count-by-member/{memberId}")
    public Result<Integer> countCommentsByMember(@PathVariable("memberId") Long memberId) {
        return Result.success(productInternalService.countCommentsByMember(memberId));
    }

    private PortalProductListDto toPortalProductListDto(PortalProductListVo vo) {
        PortalProductListDto dto = new PortalProductListDto();
        BeanUtils.copyProperties(vo, dto);
        return dto;
    }
}
