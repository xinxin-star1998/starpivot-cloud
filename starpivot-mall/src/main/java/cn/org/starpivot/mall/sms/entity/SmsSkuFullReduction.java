package cn.org.starpivot.mall.sms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sms_sku_full_reduction")
public class SmsSkuFullReduction {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long skuId;

    private BigDecimal fullPrice;

    private BigDecimal reducePrice;

    private Integer addOther;

}
