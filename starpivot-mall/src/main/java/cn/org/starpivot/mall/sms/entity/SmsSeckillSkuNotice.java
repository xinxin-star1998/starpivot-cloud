package cn.org.starpivot.mall.sms.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sms_seckill_sku_notice")
public class SmsSeckillSkuNotice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    private Long skuId;

    private Long sessionId;

    private LocalDateTime subscribeTime;

    private LocalDateTime sendTime;

    private Integer noticeType;

}
