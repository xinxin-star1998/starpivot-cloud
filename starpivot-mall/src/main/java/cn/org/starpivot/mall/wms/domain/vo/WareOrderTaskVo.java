package cn.org.starpivot.mall.wms.domain.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class WareOrderTaskVo {

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

    private List<WareOrderTaskDetailVo> details;
}
