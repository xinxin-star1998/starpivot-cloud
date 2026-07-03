package cn.org.starpivot.mall.sms.domain.bo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HomeCategoryHotSaveBo {

    private Long id;

    @NotNull(message = "关联分类不能为空")
    private Long catId;

    @Size(max = 200, message = "展示标题长度不能超过200")
    private String title;

    @Size(max = 512, message = "图标地址长度不能超过512")
    private String icon;

    @Size(max = 512, message = "链接地址长度不能超过512")
    private String url;

    private Integer status;

    private Integer sort;

    @Size(max = 512, message = "备注长度不能超过512")
    private String note;
}
