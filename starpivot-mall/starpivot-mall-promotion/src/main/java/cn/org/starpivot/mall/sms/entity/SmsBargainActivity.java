package cn.org.starpivot.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 砍价活动实体。
 * <p>
 * 对应数据库表 {@code sms_bargain_activity}。
 * </p>
 */
@Data
@TableName("sms_bargain_activity")
public class SmsBargainActivity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private Long skuId;

    /** 商品原价 */
    private BigDecimal originalPrice;

    /** 砍价底价 */
    private BigDecimal floorPrice;

    /** 最多助力次数 */
    private Integer bargainTimes;

    /** 单次最低砍价金额 */
    private BigDecimal minAmount;

    /** 单次最高砍价金额 */
    private BigDecimal maxAmount;

    /** 活动库存 */
    private Integer stockCount;

    /** 每人限购件数 */
    private Integer limitBuy;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 发起砍价有效小时数 */
    private Integer validHours;

    /** 0下线 1上线 */
    private Integer status;

    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long userId;
}
