package cn.org.starpivot.mall.pms.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 品牌新增/编辑 DTO。
 */
@Data
public class PmsBrandDTO {

    private Long brandId;

    @NotBlank(message = "品牌名称不能为空")
    @Size(max = 50, message = "品牌名称长度不能超过50个字符")
    private String name;

    private String logo;

    private String descript;

    private Integer showStatus;

    @Size(max = 1, message = "检索首字母长度不能超过1个字符")
    private String firstLetter;

    private Integer sort;
}
