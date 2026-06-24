package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategorySaveBo {

    private Long catId;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 128, message = "分类名称长度不能超过128")
    private String name;

    private Long parentCid;
    private Integer sort;

    @NotNull(message = "显示状态不能为空")
    private Integer showStatus;

    @Size(max = 512, message = "图标长度不能超过512")
    private String icon;

    @Size(max = 32, message = "计量单位长度不能超过32")
    private String productUnit;
}
