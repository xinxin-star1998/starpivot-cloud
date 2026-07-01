package cn.org.starpivot.mall.pms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** SKU 分页查询（pms_sku_info） */

/**
 * SKU查询请求 BO。
 * <p>
 * 用于分页查询或列表筛选的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link EqualsAndHashCode} — 继承父类字段参与 equals/hashCode</li>
 * </ul>
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class SkuReqBo extends PageReqBo {

    /** SKU 名称模糊 */
    /**
     * sku名称
     */
    private String skuName;

    /** SPU 名称模糊 */
    /**
     * spu名称
     */
    private String spuName;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * Catalog ID
     */
    private Long catalogId;

    /**
     * 品牌 ID
     */
    private Long brandId;

    /** 所属 SPU 上架状态：0-下架 1-上架 */
    /**
     * 状态
     */
    private Integer spuPublishStatus;

    /** 价格下限（含） */
    private BigDecimal minPrice;

    /** 价格上限（含） */
    private BigDecimal maxPrice;
}
