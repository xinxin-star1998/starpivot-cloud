package cn.org.starpivot.api.fallback;

import cn.org.starpivot.api.mall.stock.MallStockInternalClient;
import org.springframework.cloud.openfeign.FallbackFactory;

public class MallStockInternalClientFallbackFactory implements FallbackFactory<MallStockInternalClient> {

    private static final String ACTION = "商城库存服务";

    @Override
    public MallStockInternalClient create(Throwable cause) {
        return skuIds -> FeignFallbackSupport.unavailable(cause, ACTION);
    }
}
