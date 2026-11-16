package cn.org.starpivot.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 砍价记录实体。
 * <p>
 * 对应数据库表 {@code sms_bargain_record}，一行代表一位会员发起的一次砍价。
 * </p>
 */
@Data
@TableName("sms_bargain_record")
public class SmsBargainRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;

    /** 发起会员 */
    private Long memberId;

    private Long skuId;

    private BigDecimal originalPrice;

    /** 当前砍后价 */
    private BigDecimal currentPrice;

    private BigDecimal floorPrice;

    /** 0砍价中 1已到底 2已下单 3已失效 */
    private Integer status;

    /** 砍价截止时间 */
    private LocalDateTime expireTime;

    private Long orderId;

    private String orderSn;

    private LocalDateTime payTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
