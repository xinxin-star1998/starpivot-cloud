package cn.org.starpivot.mall.pms.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商品分类新增/编辑 DTO。
 */
@Data
public class PmsCategoryDTO {

    private Long catId;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    private String name;

    private Long parentCid;

    private Integer catLevel;

    private Integer showStatus;

    private Integer sort;

    private String icon;

    private String productUnit;

    private Integer productCount;
}
