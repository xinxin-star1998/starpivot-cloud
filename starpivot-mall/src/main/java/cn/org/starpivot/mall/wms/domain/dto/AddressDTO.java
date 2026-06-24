package cn.org.starpivot.mall.wms.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 省市区地址新增/修改
 */
@Data
public class AddressDTO {

    /** 修改时必填 */
    private Long id;

    @NotBlank(message = "地区编码不能为空")
    @Size(max = 32, message = "地区编码长度不能超过32")
    private String code;

    @Size(max = 32, message = "父级编码长度不能超过32")
    private String parentCode;

    @NotBlank(message = "地区名称不能为空")
    @Size(max = 255, message = "地区名称长度不能超过255")
    private String name;

    @NotNull(message = "地区层级不能为空")
    private Long level;
}
