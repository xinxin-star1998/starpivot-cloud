package cn.org.starpivot.mall.portal.domain.bo;

import lombok.Data;

import java.util.List;

/** 结算页价格试算请求 */
@Data
public class PortalOrderPriceTrialBo {

    /** true=从购物车勾选商品试算 */
    private Boolean useCart = Boolean.TRUE;

    private List<Long> cartSkuIds;

    private List<PortalOrderItemBo> items;

    /** 优惠券领取记录 ID */
    private Long couponHistoryId;

    /** 使用积分数量，null/0 表示不使用 */
    private Integer useIntegration;
}