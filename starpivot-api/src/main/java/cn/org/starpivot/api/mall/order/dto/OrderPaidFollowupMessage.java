package cn.org.starpivot.api.mall.order.dto;



import lombok.Data;



import java.io.Serializable;



/**

 * 订单支付成功后营销/会员跟进事件（优惠券确认、积分成长值发放）。

 */

@Data

public class OrderPaidFollowupMessage implements Serializable {



    private Long orderId;



    private String orderSn;

}

