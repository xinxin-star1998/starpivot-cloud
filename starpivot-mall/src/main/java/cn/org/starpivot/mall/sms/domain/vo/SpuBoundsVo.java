package cn.org.starpivot.mall.sms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * SPU 积分/成长值视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class SpuBoundsVo {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * SPU ID
     */
    private Long spuId;
    /**
     * spu名称
     */
    private String spuName;
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
