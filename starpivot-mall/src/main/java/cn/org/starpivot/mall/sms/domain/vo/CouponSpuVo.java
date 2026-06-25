package cn.org.starpivot.mall.sms.domain.vo;

import lombok.Data;

/**
 * 优惠券 SPU视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class CouponSpuVo {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 优惠券 ID
     */
    private Long couponId;
    /**
     * SPU ID
     */
    private Long spuId;
    /**
     * spu名称
     */
    private String spuName;
}
