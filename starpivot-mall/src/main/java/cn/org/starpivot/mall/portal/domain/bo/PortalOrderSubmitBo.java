package cn.org.starpivot.mall.portal.domain.bo;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class PortalOrderSubmitBo {

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    /** true=从购物车勾选商品下单，false=使用 items */
    private Boolean useCart = Boolean.TRUE;

    /** useCart=true 时可选，指定购物车 SKU；为空则提交全部已勾选 */
    private List<Long> cartSkuIds;

    /** useCart=false 时必填 */
    private List<PortalOrderItemBo> items;

    private String note;

    /** 支付方式，默认 1（在线支付） */
    private Integer payType;
}
