package cn.org.starpivot.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 秒杀 SKU 通知实体。
 * <p>
 * 对应数据库表 {@code sms_seckill_sku_notice}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("sms_seckill_sku_notice")
public class SmsSeckillSkuNotice {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员 ID
     */
    private Long memberId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * Session ID
     */
    private Long sessionId;

    /**
     * subscribe时间
     */
    private LocalDateTime subscribeTime;

    /**
     * send时间
     */
    private LocalDateTime sendTime;

    /**
     * 类型
     */
    private Integer noticeType;

}
