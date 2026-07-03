package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

/**
 * 订单提交视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PortalOrderSubmitVo {

    /**
     * Order ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 状态
     */
    private Integer status;
}
