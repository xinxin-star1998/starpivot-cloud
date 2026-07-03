package cn.org.starpivot.api.mall.ware;

import cn.org.starpivot.api.fallback.MallWareInternalClientFallbackFactory;
import cn.org.starpivot.api.mall.ware.dto.WareSkuAddStockRequest;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 商城仓储内部接口（供 product 服务初始化 SKU 库存等）。
 */
@FeignClient(
        name = "starpivot-mall-ware",
        contextId = "mallWareInternalClient",
        path = "/api/${starpivot.api.version:v1}",
        fallbackFactory = MallWareInternalClientFallbackFactory.class)
public interface MallWareInternalClient {

    @PostMapping("/internal/ware/sku/add-stock")
    Result<Void> addStock(@RequestBody WareSkuAddStockRequest request);
}
