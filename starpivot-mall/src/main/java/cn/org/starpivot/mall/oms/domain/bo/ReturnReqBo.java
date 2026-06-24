package cn.org.starpivot.mall.oms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 退货申请列表查询（oms_order_return_apply）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReturnReqBo extends PageReqBo {

    private String orderSn;

    private String memberUsername;

    /** 0待处理 1退货中 2已完成 3已拒绝 */
    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
