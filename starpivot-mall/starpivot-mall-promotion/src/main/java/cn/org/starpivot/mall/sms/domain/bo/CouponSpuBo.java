package cn.org.starpivot.mall.sms.domain.bo;

import lombok.Data;

/**
 * 优惠券 SPU请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class CouponSpuBo {

    /**
     * SPU ID
     */
    private Long spuId;
    /**
     * spu名称
     */
    private String spuName;
}
