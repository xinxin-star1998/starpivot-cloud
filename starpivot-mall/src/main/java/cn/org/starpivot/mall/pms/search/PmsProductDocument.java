package cn.org.starpivot.mall.pms.search;

import lombok.Data;

/**
 * 商品 ES 文档（索引 mall_product）。
 */
@Data
public class PmsProductDocument {

    private Long id;

    private String spuName;

    private String spuDescription;

    private Long catalogId;

    private Long brandId;

    private String brandName;

    private Double price;

    private String coverImg;

    private Integer publishStatus;

    /** 创建时间毫秒时间戳，用于排序 */
    private Long createTime;
}
