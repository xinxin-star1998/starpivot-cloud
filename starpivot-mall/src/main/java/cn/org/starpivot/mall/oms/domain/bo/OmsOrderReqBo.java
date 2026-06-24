package cn.org.starpivot.mall.oms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 订单列表查询（oms_order）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OmsOrderReqBo extends PageReqBo {

    /** 订单号（模糊） */
    private String orderSn;

    /** 会员用户名（模糊） */
    private String memberUsername;

    /** 订单状态：0待付款 1待发货 2已发货 3已完成 4已关闭 5无效 */
    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
