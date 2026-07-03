package cn.org.starpivot.mall.common;

import cn.org.starpivot.api.mall.stock.MallStockInternalClient;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MallStockFeignSupport {

    private static final String ACTION = "商城库存服务";

    private final MallStockInternalClient mallStockInternalClient;

    public Map<Long, Integer> requireAvailableStockMap(Collection<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Map.of();
        }
        Result<Map<Long, Integer>> result = mallStockInternalClient.getAvailableStockMap(skuIds);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new BizException(result != null ? result.getMessage() : ACTION + "不可用");
        }
        return result.getData();
    }
}
