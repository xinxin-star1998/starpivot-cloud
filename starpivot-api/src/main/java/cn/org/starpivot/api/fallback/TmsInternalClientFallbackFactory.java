package cn.org.starpivot.api.fallback;

import cn.org.starpivot.api.tms.TmsInternalClient;
import cn.org.starpivot.api.tms.dto.FreightCalculateRequest;
import cn.org.starpivot.api.tms.dto.FreightCalculateResult;
import cn.org.starpivot.api.tms.vo.ShipmentTrackingVo;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FallbackFactory;

public class TmsInternalClientFallbackFactory implements FallbackFactory<TmsInternalClient> {

    private static final String ACTION = "TMS 物流服务";

    @Override
    public TmsInternalClient create(Throwable cause) {
        return new TmsInternalClient() {
            @Override
            public Result<ShipmentTrackingVo> getTracking(String bizModule, String bizType, Long bizId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<ShipmentTrackingVo> refreshTrack(Long shipmentId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<FreightCalculateResult> calculateFreight(FreightCalculateRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }
        };
    }
}
