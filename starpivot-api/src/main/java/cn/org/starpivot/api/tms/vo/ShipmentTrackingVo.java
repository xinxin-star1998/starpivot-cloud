package cn.org.starpivot.api.tms.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShipmentTrackingVo implements Serializable {

    private Long shipmentId;
    private String shipmentSn;
    private String orderSn;
    private String carrierName;
    private String kuaidi100Com;
    private String trackingNo;
    private String status;
    private LocalDateTime shipTime;
    private LocalDateTime deliverTime;
    private List<TrackEventVo> events;
}
