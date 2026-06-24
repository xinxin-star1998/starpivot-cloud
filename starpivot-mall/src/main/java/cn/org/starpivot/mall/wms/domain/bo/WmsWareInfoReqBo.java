package cn.org.starpivot.mall.wms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 仓库列表查询（wms_ware_info）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WmsWareInfoReqBo extends PageReqBo {

    private Long id;

    /** 仓库名称（模糊） */
    private String name;

    /** 详细地址（模糊） */
    private String address;

    /** 区域编码 */
    private String areacode;
}
