package cn.org.starpivot.mall.portal.domain.model;

import lombok.Data;

/**
 * Redis 购物车条目（仅存 skuId / quantity / checked）。
 */
@Data
public class PortalCartEntry {

    private Long skuId;

    private Integer quantity;

    /** 是否选中结算 */
    private Boolean checked = Boolean.TRUE;
}
