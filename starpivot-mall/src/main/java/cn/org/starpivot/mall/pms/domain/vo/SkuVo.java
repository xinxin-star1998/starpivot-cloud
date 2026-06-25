package cn.org.starpivot.mall.pms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** SKU 列表/详情 VO */

/**
 * SKU视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class SkuVo {

    /**
     * SKU ID
     */
    private Long skuId;
    /**
     * SPU ID
     */
    private Long spuId;
    /**
     * spu名称
     */
    private String spuName;
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

    /** 所属 SPU 上架状态：0-下架 1-上架 */
    /**
     * 状态
     */
    private Integer spuPublishStatus;

    /** 销售属性展示：颜色:黑色 内存:8GB */
    /**
     * sale Attr Text
     */
    private String saleAttrText;

    /**
     * sale Attrs
     */
    private List<Attr> saleAttrs;
}
