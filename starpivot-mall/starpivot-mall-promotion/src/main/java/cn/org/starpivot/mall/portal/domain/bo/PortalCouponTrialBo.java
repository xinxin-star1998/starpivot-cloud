package cn.org.starpivot.mall.portal.domain.bo;

import lombok.Data;

import java.util.List;

/** 结算页优惠券试算上下文 */
@Data
public class PortalCouponTrialBo {

    private List<PortalOrderItemBo> items;

    /** 与 submit 一致：true 时从购物车勾选商品试算 */
    private Boolean useCart = Boolean.TRUE;

    private List<Long> cartSkuIds;
}
