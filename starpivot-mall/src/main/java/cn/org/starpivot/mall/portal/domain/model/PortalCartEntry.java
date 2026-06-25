package cn.org.starpivot.mall.portal.domain.model;

import lombok.Data;

/**
 * Cartentry请求 BO。
 */
@Data
public class PortalCartEntry {

    private Long skuId;

    private Integer quantity;

    /** 是否选中结算 */
    private Boolean checked = Boolean.TRUE;
}
