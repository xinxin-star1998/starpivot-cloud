package cn.org.starpivot.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * SPU 积分/成长值实体。
 * <p>
 * 对应数据库表 {@code sms_spu_bounds}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("sms_spu_bounds")
public class SmsSpuBounds {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * grow Bounds
     */
    private BigDecimal growBounds;

    /**
     * buy Bounds
     */
    private BigDecimal buyBounds;

    /**
     * work
     */
    private Integer work;

}
