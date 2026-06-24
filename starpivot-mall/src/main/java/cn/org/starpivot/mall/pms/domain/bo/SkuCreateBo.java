package cn.org.starpivot.mall.pms.domain.bo;

import cn.org.starpivot.mall.pms.domain.vo.Attr;
import cn.org.starpivot.mall.pms.domain.vo.Images;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/** SKU 独立新增（挂载已有 SPU） */
@Data
public class SkuCreateBo {

    @NotNull(message = "SPU ID 不能为空")
    private Long spuId;

    @NotBlank(message = "SKU 名称不能为空")
    @Size(max = 255, message = "SKU 名称长度不能超过255")
    private String skuName;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.0", inclusive = true, message = "价格不能为负数")
    private BigDecimal price;

    private String skuTitle;

    private String skuSubtitle;

    /** 默认图（objectKey 或 URL） */
    private String skuDefaultImg;

    /** 销售属性组合 */
    private List<Attr> attr;

    /** SKU 图片 */
    private List<Images> images;
}
