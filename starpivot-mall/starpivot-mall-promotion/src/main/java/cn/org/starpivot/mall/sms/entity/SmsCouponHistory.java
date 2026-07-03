package cn.org.starpivot.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 优惠券领取历史实体。
 * <p>
 * 对应数据库表 {@code sms_coupon_history}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("sms_coupon_history")
public class SmsCouponHistory {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
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
