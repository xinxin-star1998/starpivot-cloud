package cn.org.starpivot.mall.portal.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品列表视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PortalProductListVo {

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
     * brand名称
     */
    private String brandName;

    /**
     * price
     */
    private BigDecimal price;

    /**
     * 图片
     */
    private String coverImg;

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
}
