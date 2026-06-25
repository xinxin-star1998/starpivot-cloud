package cn.org.starpivot.mall.sms.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 优惠券发布状态请求 BO。
 */

@Data
public class CouponPublishBo {

    @NotNull(message = "优惠券ID不能为空")
    private Long id;

    /** 0-未发布 1-已发布 */
    @NotNull(message = "发布状态不能为空")
    private Integer publish;
}
