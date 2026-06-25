package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


/**
 * 购物车视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PortalCartVo {

    /**
     * items
     */
    private List<PortalCartItemVo> items;

    /**
     * checked数量
     */
    private Integer checkedCount;

    /**
     * 金额
     */
    private BigDecimal checkedAmount;
}
