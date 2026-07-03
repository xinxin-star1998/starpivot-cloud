package cn.org.starpivot.mall.sms.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 优惠券领取历史视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class CouponHistoryVo {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 优惠券 ID
     */
    private Long couponId;
    /**
     * 会员 ID
     */
    private Long memberId;
    /**
     * memberNick名称
     */
    private String memberNickName;
    /**
     * 类型
     */
    private Integer getType;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 类型
     */
    private Integer useType;
    /**
     * use时间
     */
    private LocalDateTime useTime;
    /**
     * Order ID
     */
    private Long orderId;
    /**
     * 订单编号
     */
    private String orderSn;
}
