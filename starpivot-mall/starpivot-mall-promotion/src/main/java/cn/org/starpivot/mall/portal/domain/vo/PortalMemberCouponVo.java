package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** C 端可用优惠券（下单试算/选用） */
@Data
public class PortalMemberCouponVo {

    private Long historyId;
    private Long couponId;
    private String couponName;
    private BigDecimal amount;
    private BigDecimal minPoint;
    private Integer useType;
    private LocalDateTime endTime;
}
