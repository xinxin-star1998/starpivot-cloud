package cn.org.starpivot.mall.sms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sms_seckill_sku_relation")
public class SmsSeckillSkuRelation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long promotionId;

    private Long promotionSessionId;

    private Long skuId;

    private BigDecimal seckillPrice;

    private Integer seckillCount;

    private Integer seckillLimit;

    private Integer seckillSort;

}
