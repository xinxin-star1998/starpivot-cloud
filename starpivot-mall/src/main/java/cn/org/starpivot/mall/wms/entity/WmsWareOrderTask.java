package cn.org.starpivot.mall.wms.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("wms_ware_order_task")
public class WmsWareOrderTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String orderSn;

    private String consignee;

    private String consigneeTel;

    private String deliveryAddress;

    private String orderComment;

    private Integer paymentWay;

    private Integer taskStatus;

    private String orderBody;

    private String trackingNo;

    private LocalDateTime createTime;

    private Long wareId;

    private String taskComment;

}
