package cn.org.starpivot.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拼团记录实体。
 * <p>
 * 对应数据库表 {@code sms_group_buy_record}，一行代表一位参团会员。
 * </p>
 */
@Data
@TableName("sms_group_buy_record")
public class SmsGroupBuyRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;

    /** 参团会员 */
    private Long memberId;

    /** 团长会员，非团长为空 */
    private Long leaderMemberId;

    private Long skuId;

    /** 团号，同一团的所有记录共享 */
    private String groupNo;

    private Integer groupSize;

    private BigDecimal groupPrice;

    private Integer quantity;

    /** 0待支付 1待成团 2已成团 3成团失败 4已取消 */
    private Integer status;

    /** 成团截止时间 */
    private LocalDateTime expireTime;

    /** 支付截止时间 */
    private LocalDateTime payExpireTime;

    private Long orderId;

    private String orderSn;

    private LocalDateTime payTime;

    private LocalDateTime completeTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 是否为团长记录。
     *
     * @return true 表示该记录由团长创建
     */
    public boolean isLeader() {
        return leaderMemberId != null && leaderMemberId.equals(memberId);
    }
}
