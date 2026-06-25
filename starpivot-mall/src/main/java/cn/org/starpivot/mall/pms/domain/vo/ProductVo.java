package cn.org.starpivot.mall.pms.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** SPU 详情 VO（含发布向导关联数据） */

/**
 * 商品视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class ProductVo {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * spu名称
     */
    private String spuName;
    /**
     * spu Description
     */
    private String spuDescription;
    /**
     * Catalog ID
     */
    private Long catalogId;
    /**
     * 品牌 ID
     */
    private Long brandId;
    /**
     * weight
     */
    private BigDecimal weight;
    /**
     * 状态
     */
    private Integer publishStatus;

    /**
     * 创建时间
     */
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /** 商品详情图 URL 列表 */
    /**
     * decript
     */
    private List<String> decript;

    /** SPU 图集 URL 列表 */
    /**
     * images
     */
    private List<String> images;

    /** 列表展示用封面（图集中默认图或首张） */
    /**
     * 图片
     */
    private String coverImg;

    /**
     * base Attrs
     */
    private List<BaseAttrs> baseAttrs;

    /**
     * skus
     */
    private List<Skus> skus;
}
