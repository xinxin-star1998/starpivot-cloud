package cn.org.starpivot.mall.pms.domain.bo;

import cn.org.starpivot.mall.pms.domain.vo.Attr;
import cn.org.starpivot.mall.pms.domain.vo.Images;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** SKU 独立新增（挂载已有 SPU） */

/**
 * SKU 创建请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class SkuCreateBo {

    /**
     * SPU ID
     */
    /**
     * SPU ID
     */
    @NotNull(message = "SPU ID 不能为空")
    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * sku名称
     */
    /**
     * sku名称
     */
    @NotBlank(message = "SKU 名称不能为空")
    @Size(max = 255, message = "SKU 名称长度不能超过255")
    /**
     * sku名称
     */
    private String skuName;

    /**
     * price
     */
    /**
     * price
     */
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.0", inclusive = true, message = "价格不能为负数")
    /**
     * price
     */
    private BigDecimal price;

    /**
     * sku Title
     */
    private String skuTitle;

    /**
     * sku Subtitle
     */
    private String skuSubtitle;

    /** 默认图（objectKey 或 URL） */
    /**
     * 图片
     */
    private String skuDefaultImg;

    /** 销售属性组合 */
    /**
     * attr
     */
    private List<Attr> attr;

    /** SKU 图片 */
    /**
     * images
     */
    private List<Images> images;
}
