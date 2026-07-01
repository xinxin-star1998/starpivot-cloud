package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 首页商品视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PortalHomeProductVo {

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * spu名称
     */
    private String spuName;

    /**
     * 图片
     */
    private String coverImg;

    /** 原价 / 常规售价 */
    /**
     * price
     */
    private BigDecimal price;

    /** 促销价（秒杀价等） */
    /**
     * promo Price
     */
    private BigDecimal promoPrice;

    /** 秒杀剩余库存（仅秒杀场景） */
    private Integer seckillStockRemain;

    /** 秒杀限购（仅秒杀场景） */
    private Integer seckillLimit;
}
