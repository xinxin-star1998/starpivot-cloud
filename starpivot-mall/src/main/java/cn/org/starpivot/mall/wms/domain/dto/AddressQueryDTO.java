package cn.org.starpivot.mall.wms.domain.dto;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 省市区地址列表查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AddressQueryDTO extends PageReqBo {

    /** 地区编码（精确） */
    private String code;

    /** 父级编码（精确） */
    private String parentCode;

    /** 地区名称（模糊） */
    private String name;

    /** 层级：0-省 1-市 2-区县 3-乡镇 */
    private Long level;
}
