package cn.org.starpivot.mall.sms.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class HomeSubjectSaveBo {

    private Long id;

    @NotBlank(message = "专题名称不能为空")
    @Size(max = 200, message = "专题名称长度不能超过200")
    private String name;

    @Size(max = 255, message = "标题长度不能超过255")
    private String title;

    @Size(max = 255, message = "副标题长度不能超过255")
    private String subTitle;

    private Integer status;
    private String url;
    private Integer sort;

    @Size(max = 500, message = "图片地址长度不能超过500")
    private String img;

    private List<HomeSubjectSpuBo> spuList;
}
