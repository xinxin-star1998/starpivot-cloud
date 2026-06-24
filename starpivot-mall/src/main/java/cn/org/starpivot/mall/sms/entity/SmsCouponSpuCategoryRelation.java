package cn.org.starpivot.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sms_coupon_spu_category_relation")
public class SmsCouponSpuCategoryRelation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long couponId;

    private Long categoryId;

    private String categoryName;

}
