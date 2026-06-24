package cn.org.starpivot.mall.pms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SPU 列表查询（pms_spu_info）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductReqBo extends PageReqBo {

    /** SPU 名称模糊 */
    private String spuName;

    private Long catalogId;

    private Long brandId;

    /** 上架状态等，不传则不过滤 */
    private Integer publishStatus;
}
