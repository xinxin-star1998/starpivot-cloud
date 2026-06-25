package cn.org.starpivot.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体。
 * <p>
 * 对应数据库表 {@code sms_coupon}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("sms_coupon")
public class SmsCoupon {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 优惠券类型
     */
    private Integer couponType;

    /**
     * 图片
     */
    private String couponImg;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * num
     */
    private Integer num;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * per Limit
     */
    private Integer perLimit;

    /**
     * min Point
     */
    private BigDecimal minPoint;

    /**
     * start时间
     */
    private LocalDateTime startTime;

    /**
     * end时间
     */
    private LocalDateTime endTime;

    /**
     * 类型
     */
    private Integer useType;

    /**
     * note
     */
    private String note;

    /**
     * publish数量
     */
    private Integer publishCount;

    /**
     * use数量
     */
    private Integer useCount;

    /**
     * receive数量
     */
    private Integer receiveCount;

    /**
     * enableStart时间
     */
    private LocalDateTime enableStartTime;

    /**
     * enableEnd时间
     */
    private LocalDateTime enableEndTime;

    /**
     * code
     */
    private String code;

    /**
     * member Level
     */
    private Integer memberLevel;

    /**
     * publish
     */
    private Integer publish;

}
