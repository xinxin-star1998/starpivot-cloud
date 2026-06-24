package cn.org.starpivot.mall.sms.domain.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CouponHistoryVo {

    private Long id;
    private Long couponId;
    private Long memberId;
    private String memberNickName;
    private Integer getType;
    private LocalDateTime createTime;
    private Integer useType;
    private LocalDateTime useTime;
    private Long orderId;
    private String orderSn;
}
