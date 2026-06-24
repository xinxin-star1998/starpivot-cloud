package cn.org.starpivot.mall.sms.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sms_coupon_history")
public class SmsCouponHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long couponId;

    private Long memberId;

    private String memberNickName;

    private Integer getType;

    private LocalDateTime createTime;

    private Integer useType;

    private LocalDateTime useTime;

    private Long orderId;

    private String orderSn;

}
