package cn.org.starpivot.mall.sms.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sms_seckill_promotion")
public class SmsSeckillPromotion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private LocalDateTime createTime;

    private Long userId;

}
