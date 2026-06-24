package cn.org.starpivot.mall.sms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CouponHistoryReqBo extends PageReqBo {

    private Long memberId;
    private Long couponId;
}
