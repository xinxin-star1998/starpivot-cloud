package cn.org.starpivot.mall.sms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sms_member_price")
public class SmsMemberPrice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long skuId;

    private Long memberLevelId;

    private String memberLevelName;

    private BigDecimal memberPrice;

    private Integer addOther;

}
