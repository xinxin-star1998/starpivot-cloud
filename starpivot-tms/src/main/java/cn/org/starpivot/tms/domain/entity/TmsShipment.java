package cn.org.starpivot.tms.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tms_shipment")
public class TmsShipment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String shipmentSn;
    private String bizModule;
    private String bizType;
    private Long bizId;
    private String bizKey;
    private String orderSn;
    private Long carrierId;
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
    private LocalDateTime updateTime;
    @TableLogic
    private String delFlag;
}
