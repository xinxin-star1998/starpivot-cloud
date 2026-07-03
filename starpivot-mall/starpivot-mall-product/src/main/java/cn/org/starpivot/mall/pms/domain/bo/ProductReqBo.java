package cn.org.starpivot.mall.pms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 商品查询请求 BO。
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
public class ProductReqBo extends PageReqBo {

    /** SPU 名称模糊 */
    /**
     * spu名称
     */
    private String spuName;

    /**
     * Catalog ID
     */
    private Long catalogId;

    /**
     * 品牌 ID
     */
    private Long brandId;

    /** 上架状态等，不传则不过滤 */
    /**
     * 状态
     */
    private Integer publishStatus;
}
