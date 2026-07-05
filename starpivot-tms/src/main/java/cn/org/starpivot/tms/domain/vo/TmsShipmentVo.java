package cn.org.starpivot.tms.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TmsShipmentVo {
    private Long id;
    private String shipmentSn;
    private String orderSn;
    private Long bizId;
    private String carrierName;
    private String kuaidi100Com;
    private String trackingNo;
    private String status;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private LocalDateTime shipTime;
    private LocalDateTime deliverTime;
    private LocalDateTime createTime;
    private List<TmsTrackEventVo> events;
}
