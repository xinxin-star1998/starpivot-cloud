package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** 结算页价格试算结果 */
@Data
public class PortalOrderPriceTrialVo {

    /** 商品原价合计 */
    private BigDecimal originalAmount;

    /** 促销优惠（不含优惠券、积分） */
    private BigDecimal promotionAmount;

    /** 促销后商品金额 */
    private BigDecimal merchandiseAmount;

    /** 优惠券抵扣 */
    private BigDecimal couponAmount;

    /** 积分抵扣金额 */
    private BigDecimal integrationAmount;

    /** 使用积分数量 */
    private Integer useIntegration;

    /** 会员当前可用积分 */
    private Integer availableIntegration;

    /** 本单最多可用积分 */
    private Integer maxUsableIntegration;

    /** 运费 */
    private BigDecimal freightAmount;

    /** 是否免邮 */
    private Boolean freeFreight;

    /** 应付总额 */
    private BigDecimal payAmount;

    private List<PortalOrderPriceLineVo> lines;
}
