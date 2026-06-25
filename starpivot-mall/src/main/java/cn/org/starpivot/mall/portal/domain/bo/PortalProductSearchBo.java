package cn.org.starpivot.mall.portal.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Productsearch请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link EqualsAndHashCode} — 继承父类字段参与 equals/hashCode</li>
 * </ul>
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class PortalProductSearchBo extends PageReqBo {

    /** SPU 名称关键字 */
    /**
     * keyword
     */
    private String keyword;

    /** 三级分类 ID */
    /**
     * Catalog ID
     */
    private Long catalogId;

    /** 品牌 ID */
    /**
     * 品牌 ID
     */
    private Long brandId;

    /** 排序：default | priceAsc | priceDesc | newest */
    /**
     * 排序
     */
    private String sort;
}
