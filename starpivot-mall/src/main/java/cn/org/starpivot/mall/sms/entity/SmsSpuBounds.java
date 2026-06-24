package cn.org.starpivot.mall.sms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sms_spu_bounds")
public class SmsSpuBounds {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long spuId;

    private BigDecimal growBounds;

    private BigDecimal buyBounds;

    private Integer work;

}
