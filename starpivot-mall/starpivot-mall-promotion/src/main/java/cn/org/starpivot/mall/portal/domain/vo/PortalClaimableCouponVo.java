package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** C 端可领取优惠券 */
@Data
public class PortalClaimableCouponVo {

    private Long couponId;
    private String couponName;
    private BigDecimal amount;
    private BigDecimal minPoint;
    private Integer useType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime enableEndTime;
    private Integer perLimit;
    /** 当前会员已领取数量 */
    private Integer receivedCount;
    /** 是否还可领取 */
    private Boolean canReceive;
}
