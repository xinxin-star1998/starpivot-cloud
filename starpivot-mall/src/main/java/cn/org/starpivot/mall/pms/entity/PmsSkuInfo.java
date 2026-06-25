package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * SKU实体。
 * <p>
 * 对应数据库表 {@code pms_sku_info}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("pms_sku_info")
public class PmsSkuInfo {

    /**
     * SKU ID
     */

    @TableId(value = "sku_id", type = IdType.AUTO)
    private Long skuId;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * sku名称
     */
    private String skuName;

    /**
     * sku Desc
     */
    private String skuDesc;

    /**
     * Catalog ID
     */
    private Long catalogId;

    /**
     * 品牌 ID
     */
    private Long brandId;

    /**
     * 图片
     */
    private String skuDefaultImg;

    /**
     * sku Title
     */
    private String skuTitle;

    /**
     * sku Subtitle
     */
    private String skuSubtitle;

    /**
     * price
     */
    private BigDecimal price;

    /**
     * sale数量
     */
    private Long saleCount;

}
