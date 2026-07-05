package cn.org.starpivot.tms.service;

import cn.org.starpivot.tms.domain.dto.TmsShipmentShipDto;
import cn.org.starpivot.tms.domain.dto.TmsShipmentQueryDto;
import cn.org.starpivot.tms.domain.vo.TmsShipmentVo;
import cn.org.starpivot.api.tms.vo.ShipmentTrackingVo;
import cn.org.starpivot.common.entity.PageResponse;

public interface TmsShipmentService {

    PageResponse<TmsShipmentVo> pageList(TmsShipmentQueryDto query);

    TmsShipmentVo getDetail(Long shipmentId);

    Long ship(TmsShipmentShipDto dto);

    ShipmentTrackingVo getTracking(String bizModule, String bizType, Long bizId);

    ShipmentTrackingVo refreshTrack(Long shipmentId);

    /** 定时任务：批量刷新在途运单轨迹 */
    void refreshPendingTracks(int batchSize);
}
