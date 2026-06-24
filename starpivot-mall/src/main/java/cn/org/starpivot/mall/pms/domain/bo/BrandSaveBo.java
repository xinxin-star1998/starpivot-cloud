package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BrandSaveBo {

    private Long brandId;

    @NotBlank(message = "品牌名称不能为空")
    @Size(max = 128, message = "品牌名称长度不能超过128")
    private String name;

    @Size(max = 512, message = "Logo 长度不能超过512")
    private String logo;

    private String descript;
    private Integer sort;

    @NotNull(message = "显示状态不能为空")
    private Integer showStatus;

    @Size(max = 1, message = "首字母为单字符")
    private String firstLetter;
}
