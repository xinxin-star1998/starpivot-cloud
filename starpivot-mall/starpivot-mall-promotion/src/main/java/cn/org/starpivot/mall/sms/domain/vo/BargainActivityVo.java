package cn.org.starpivot.mall.sms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 砍价活动视图对象。
 */
@Data
public class BargainActivityVo {

    private Long id;

    private String title;

    private Long skuId;

    /** SKU 名称 */
    private String skuName;

    /** SKU 主图 */
    private String skuImg;

    private BigDecimal originalPrice;

    private BigDecimal floorPrice;

    private Integer bargainTimes;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private Integer stockCount;

    private Integer limitBuy;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer validHours;

    private Integer status;

    private Integer sort;

    private LocalDateTime createTime;
}
