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
 * 商品保存请求 BO。
 * <p>
 * 用于新增或修改接口的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class ProductSaveBo {

    /** 修改时必填 */
    /**
     * 主键 ID
     */
    private Long id;
    // SPU 名称（如：小米14）
    /**
     * spu名称
     */
    /**
     * spu名称
     */
    @NotBlank(message = "SPU 名称不能为空")
    @Size(max = 200, message = "SPU 名称长度不能超过200")
    /**
     * spu名称
     */
    private String spuName;

    // SPU 简介（短描述）
    /**
     * spu Description
     */
    private String spuDescription;

    // 三级分类ID（谷粒商城用 catalogId，不是 categoryId）
    /**
     * Catalog ID
     */
    /**
     * catalog ID
     */
    @NotNull(message = "分类目录不能为空")
    /**
     * catalog ID
     */
    private Long catalogId;

    // 品牌ID
    /**
     * 品牌 ID
     */
    private Long brandId;

    // 商品重量（物流、运费计算用）
    /**
     * weight
     */
    /**
     * weight
     */
    @NotNull(message = "重量不能为空")
    @DecimalMin(value = "0.0", inclusive = true, message = "重量不能为负数")
    /**
     * weight
     */
    private BigDecimal weight;

    // 发布状态（0新建 1上架 2下架）
    /**
     * 状态
     */
    /**
     * 状态
     */
    @NotNull(message = "上架状态不能为空")
    /**
     * 状态
     */
    private int publishStatus;

    // 商品详情介绍（富文本，存到 pms_spu_info_desc）
    /**
     * decript
     */
    private List<String> decript;

    // 商品图集（图片URL列表，存到 pms_spu_images）
    /**
     * images
     */
    private List<String> images;

    // 商品积分信息（购物赠送成长值、积分）
    /**
     * bounds
     */
    private Bounds bounds;

    // 基本规格属性（例如：颜色、材质、尺寸，存到 pms_product_attr_value）
    /**
     * base Attrs
     */
    private List<BaseAttrs> baseAttrs;

    // 所有SKU信息（例如：小米14 白色128G、黑色256G...）
    /**
     * skus
     */
    private List<Skus> skus;

    /** 默认入库仓库（发布商品时 SKU 初始库存写入该仓库） */
    private Long defaultWareId;
}
