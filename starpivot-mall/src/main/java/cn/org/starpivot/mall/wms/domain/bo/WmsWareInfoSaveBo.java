package cn.org.starpivot.mall.wms.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 仓库新增/修改（wms_ware_info）
 */
@Data
public class WmsWareInfoSaveBo {

    /** 修改时必填 */
    private Long id;

    @NotBlank(message = "仓库名称不能为空")
    @Size(max = 128, message = "仓库名称长度不能超过128")
    private String name;

    @Size(max = 512, message = "详细地址长度不能超过512")
    private String address;

    @Size(max = 64, message = "区域编码长度不能超过64")
    private String areacode;
}
