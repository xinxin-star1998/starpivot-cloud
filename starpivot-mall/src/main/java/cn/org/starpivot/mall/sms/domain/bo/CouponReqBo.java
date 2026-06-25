package cn.org.starpivot.mall.sms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 优惠券查询请求 BO。
 * <p>
 * 用于分页查询或列表筛选的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link EqualsAndHashCode} — 继承父类字段参与 equals/hashCode</li>
 * </ul>
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class CouponReqBo extends PageReqBo {

    /**
     * 优惠券名称
     */
    private String couponName;
}
