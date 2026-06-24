package cn.org.starpivot.mall.wms.domain.dto;

import lombok.Data;

/**
 * 商品库存查询DTO
 * 
 * @author admin
 * @since 2026-05-22
 */
@Data
public class WmsWareSkuQueryDTO {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

    /**
     * sku_id
     */
    private Long skuId;

    /**
     * 仓库id
     */
    private Long wareId;

    /**
     * 库存数
     */
    private Long stock;

    /**
     * sku_name
     */
    private String skuName;

    /**
     * 锁定库存
     */
    private Long stockLocked;

}
