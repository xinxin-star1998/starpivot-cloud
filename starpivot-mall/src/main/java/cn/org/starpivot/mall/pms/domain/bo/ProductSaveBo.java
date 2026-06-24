package cn.org.starpivot.mall.pms.domain.bo;

import cn.org.starpivot.mall.pms.domain.vo.BaseAttrs;
import cn.org.starpivot.mall.pms.domain.vo.Bounds;
import cn.org.starpivot.mall.pms.domain.vo.Skus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * SPU 新增/修改（pms_spu_info）
 */
@Data
public class ProductSaveBo {

    /** 修改时必填 */
    private Long id;
    // SPU 名称（如：小米14）
    @NotBlank(message = "SPU 名称不能为空")
    @Size(max = 200, message = "SPU 名称长度不能超过200")
    private String spuName;

    // SPU 简介（短描述）
    private String spuDescription;

    // 三级分类ID（谷粒商城用 catalogId，不是 categoryId）
    @NotNull(message = "分类目录不能为空")
    private Long catalogId;

    // 品牌ID
    private Long brandId;

    // 商品重量（物流、运费计算用）
    @NotNull(message = "重量不能为空")
    @DecimalMin(value = "0.0", inclusive = true, message = "重量不能为负数")
    private BigDecimal weight;

    // 发布状态（0新建 1上架 2下架）
    @NotNull(message = "上架状态不能为空")
    private int publishStatus;

    // 商品详情介绍（富文本，存到 pms_spu_info_desc）
    private List<String> decript;

    // 商品图集（图片URL列表，存到 pms_spu_images）
    private List<String> images;

    // 商品积分信息（购物赠送成长值、积分）
    private Bounds bounds;

    // 基本规格属性（例如：颜色、材质、尺寸，存到 pms_product_attr_value）
    private List<BaseAttrs> baseAttrs;

    // 所有SKU信息（例如：小米14 白色128G、黑色256G...）
    private List<Skus> skus;
}
