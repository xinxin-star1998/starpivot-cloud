package cn.org.starpivot.api.mall.promotion.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/** 商品保存时同步 SPU 积分/成长值配置 */
@Data
public class SpuBoundsUpsertRequest implements Serializable {

    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
