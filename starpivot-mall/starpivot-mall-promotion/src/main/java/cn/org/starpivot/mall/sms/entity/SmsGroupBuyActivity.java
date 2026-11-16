package cn.org.starpivot.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拼团活动实体。
 * <p>
 * 对应数据库表 {@code sms_group_buy_activity}。
 * </p>
 */
@Data
@TableName("sms_group_buy_activity")
public class SmsGroupBuyActivity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 活动标题 */
    private String title;

    /** SKU ID */
    private Long skuId;

    /** 拼团价 */
    private BigDecimal groupPrice;

    /** 成团人数（含团长） */
    private Integer groupSize;

    /** 活动库存 */
    private Integer stockCount;

    /** 每人限购件数 */
    private Integer limitBuy;

    /** 活动开始时间 */
    private LocalDateTime startTime;

    /** 活动结束时间 */
    private LocalDateTime endTime;

    /** 开团后成团有效小时数 */
    private Integer validHours;

    /** 0下线 1上线 */
    private Integer status;

    /** 排序 */
    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 创建人 */
    private Long userId;
}
