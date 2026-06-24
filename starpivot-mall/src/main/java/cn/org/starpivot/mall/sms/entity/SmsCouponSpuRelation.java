package cn.org.starpivot.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sms_coupon_spu_relation")
public class SmsCouponSpuRelation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long couponId;

    private Long spuId;

    private String spuName;

}
