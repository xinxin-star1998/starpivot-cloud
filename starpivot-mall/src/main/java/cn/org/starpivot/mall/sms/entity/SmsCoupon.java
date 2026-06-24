package cn.org.starpivot.mall.sms.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sms_coupon")
public class SmsCoupon {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer couponType;

    private String couponImg;

    private String couponName;

    private Integer num;

    private BigDecimal amount;

    private Integer perLimit;

    private BigDecimal minPoint;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer useType;

    private String note;

    private Integer publishCount;

    private Integer useCount;

    private Integer receiveCount;

    private LocalDateTime enableStartTime;

    private LocalDateTime enableEndTime;

    private String code;

    private Integer memberLevel;

    private Integer publish;

}
