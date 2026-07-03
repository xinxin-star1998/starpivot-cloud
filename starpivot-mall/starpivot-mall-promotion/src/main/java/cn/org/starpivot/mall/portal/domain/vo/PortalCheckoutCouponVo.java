package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** C 端结算页优惠券选项（含不可用原因） */
@Data
@EqualsAndHashCode(callSuper = true)
public class PortalCheckoutCouponVo extends PortalMemberCouponVo {

    private LocalDateTime startTime;

    private Boolean usable;

    private String unusableReason;
}
