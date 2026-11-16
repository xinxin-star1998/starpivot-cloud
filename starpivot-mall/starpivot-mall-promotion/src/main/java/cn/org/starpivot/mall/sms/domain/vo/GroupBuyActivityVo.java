package cn.org.starpivot.mall.sms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拼团活动视图对象。
 */
@Data
public class GroupBuyActivityVo {

    private Long id;

    private String title;

    private Long skuId;

    /** SKU 名称 */
    private String skuName;

    /** SKU 主图 */
    private String skuImg;

    /** SKU 原价 */
    private BigDecimal originalPrice;

    private BigDecimal groupPrice;

    private Integer groupSize;

    private Integer stockCount;

    private Integer limitBuy;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer validHours;

    private Integer status;

    private Integer sort;

    private LocalDateTime createTime;
}
