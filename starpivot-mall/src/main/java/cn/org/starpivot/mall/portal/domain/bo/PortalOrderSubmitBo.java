package cn.org.starpivot.mall.portal.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 订单提交请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PortalOrderSubmitBo {

    /**
     * Address ID
     */
    /**
     * address ID
     */
    @NotNull(message = "收货地址不能为空")
    /**
     * address ID
     */
    private Long addressId;

    /** true=从购物车勾选商品下单，false=使用 items */
    private Boolean useCart = Boolean.TRUE;

    /** useCart=true 时可选，指定购物车 SKU；为空则提交全部已勾选 */
    /**
     * cart Sku Ids
     */
    private List<Long> cartSkuIds;

    /** useCart=false 时必填 */
    /**
     * items
     */
    private List<PortalOrderItemBo> items;

    /**
     * note
     */
    private String note;

    /** 支付方式，默认 1（在线支付） */
    private Integer payType;

    /** 使用的优惠券领取记录 ID（sms_coupon_history.id） */
    private Long couponHistoryId;
}
