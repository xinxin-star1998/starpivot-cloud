package cn.org.starpivot.api.mall.stock;

import cn.org.starpivot.api.fallback.MallStockInternalClientFallbackFactory;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;
import java.util.Map;

@FeignClient(
        name = "starpivot-mall-order",
        contextId = "mallStockInternalClient",
        path = "/api/${starpivot.api.version:v1}",
        fallbackFactory = MallStockInternalClientFallbackFactory.class)
public interface MallStockInternalClient {

    @PostMapping("/internal/mall/stock/available")
    Result<Map<Long, Integer>> getAvailableStockMap(@RequestBody Collection<Long> skuIds);
}
