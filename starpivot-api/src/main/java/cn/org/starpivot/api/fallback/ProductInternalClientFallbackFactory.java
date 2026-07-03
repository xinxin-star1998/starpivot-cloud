package cn.org.starpivot.api.fallback;

import cn.org.starpivot.api.product.ProductInternalClient;
import cn.org.starpivot.api.product.dto.*;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;
import java.util.Map;

public class ProductInternalClientFallbackFactory implements FallbackFactory<ProductInternalClient> {

    private static final String ACTION = "商品服务";

    @Override
    public ProductInternalClient create(Throwable cause) {
        return new ProductInternalClient() {
            @Override
            public Result<SkuDto> getSku(Long skuId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<List<SkuDto>> listSkusByIds(List<Long> skuIds) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Map<Long, SkuOrderSnapshotDto>> loadOrderSnapshots(List<Long> skuIds) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<SpuDto> getSpu(Long spuId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<List<SpuDto>> listSpusByIds(List<Long> spuIds) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<CategoryDto> getCategory(Long categoryId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<List<CategoryDto>> listCategoriesByIds(List<Long> categoryIds) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<List<CategoryTreeDto>> categoryTree() {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<List<BrandDto>> listBrandsByIds(List<Long> brandIds) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<List<CategoryBrandRelationDto>> listCategoryBrands(List<Long> catalogIds) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Map<Long, String>> spuCoverImages(List<Long> spuIds) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<List<SpuDto>> listNewestPublishedSpus(int limit) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<List<SkuDto>> listBudgetSkus(int limit) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Void> incrementSaleCount(SkuSaleCountRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Void> decrementSaleCount(SkuSaleCountRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<cn.org.starpivot.common.entity.PageResponse<PortalProductListDto>> listPortalProductsByOrderedSpuIds(
                    PortalProductListPageRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Integer> countPendingReviews(Long memberId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }
        };
    }
}
