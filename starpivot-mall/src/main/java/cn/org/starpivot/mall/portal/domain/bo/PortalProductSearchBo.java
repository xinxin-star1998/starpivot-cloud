package cn.org.starpivot.mall.portal.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PortalProductSearchBo extends PageReqBo {

    /** SPU 名称关键字 */
    private String keyword;

    /** 三级分类 ID */
    private Long catalogId;

    /** 品牌 ID */
    private Long brandId;

    /** 排序：default | priceAsc | priceDesc | newest */
    private String sort;
}
