package cn.org.starpivot.mall.portal.domain.bo;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PortalCommentSubmitBo {

    @NotNull(message = "商品ID不能为空")
    private Long spuId;

    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1星")
    @Max(value = 5, message = "评分最高5星")
    private Integer star;

    @NotBlank(message = "评价内容不能为空")
    @Size(max = 500, message = "评价内容最多500字")
    private String content;

    @Size(max = 2000, message = "图片地址过长")
    private String resources;
}
