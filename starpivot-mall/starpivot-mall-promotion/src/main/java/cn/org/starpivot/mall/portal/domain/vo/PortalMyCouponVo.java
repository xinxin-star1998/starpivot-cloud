package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** C 端我的优惠券 */
@Data
public class PortalMyCouponVo {

    private Long historyId;
    private Long couponId;
    private String couponName;
    private BigDecimal amount;
    private BigDecimal minPoint;
    private Integer useType;
    /** 0 未使用 / 1 已使用 / 2 已过期 */
    private Integer status;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private LocalDateTime useTime;
}
